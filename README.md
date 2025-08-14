# 트러블 슈팅 및 동시성 제어 방식에 대한 분석

# 트러블 슈팅

## 문제 상황

동일한 사용자가 총 10,000 포인트를 적립하기 위해서 동시에 10번 포인트 적립기능을 사용했는데, 기대값인 10,000 포인트보다 낮은 결과값이 나왔습니다.

## 원인 분석

동시성 문제가 발생했습니다.
동시성 문제가 발생하는 이유는 서로 다른 스레드가 동시에 공유 자원에 접근하여 변경을 시도했기 때문입니다.
여기서의 공유 자원은 특정 UserPointTable 내부의 userId에 해당하는 userPoint입니다.
userPoint에 대해서 변경은 아래와 같이 일어납니다.

1. UserPointTable에서 특정 userId에 해당하는 userPoint를 조회한다.
2. userPoint의 포인트를 충전한다.
3. 충전한 userPoint를 UserPointTable에 저장한다.

하나의 스레드만 수행한다면, 문제가 없지만 서로 다른 두개 이상의 스레드가 동시에 수행한다면, 동시성 문제 중 대표적으로 `Lost Update` 문제가 발생할 수 있습니다.
서로 다른 두 스레드가 동시에 userPoint를 조회할 경우, 두 스레드 모두 같은 시점의 포인트를 읽어 동일하게 계산한 이후에 덮어씌우는 문제가 발생합니다.

```text
스레드 21: userPoint를 조회(현재 포인트:0)
스레드 21: 포인트를 충전(현재 포인트:1000)
스레드 22: userPoint를 조회(현재 포인트:0)
스레드 22: 포인트를 충전(현재 포인트:1000)
스레드 22: userPoint를 저장(현재 포인트:1000)
스레드 21: userPoint를 저장(현재 포인트:1000) <- Lost Update 발생
```

## 해결 방안

위 동시성 이슈를 해결하기 위해서는 임계구역에 진입한 스레드가 있을 때, 다른 스레드가 접근하지 못하도록 막아야 합니다.
여기서 **임계 구역(Critical Section)은 특정 userId에 대한 userPoint의 조회-변경-저장 작업이 수행되는 코드 구간**입니다.
위 케이스에서는 UserPointTable이 ConcurrentHashMap으로 구현되어 있기 때문에, Application Level에서의 동시성 제어에 초점을 두고 문제를 살펴봤습니다.

```java

@Test
void 동일한_회원이_동시에_10번_1000_포인트_충전을_요청할_경우_10_000원을_최종적으로_반환한다() throws InterruptedException {
	// given
	final int threadCount = 10;
	final long userId = 123L;
	final long amount = 1000L;

	CountDownLatch countDownLatch = new CountDownLatch(threadCount);
	ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

	// when
	IntStream.range(0, threadCount)
		.forEach((index) -> executorService.execute(() -> {
				pointChargeService.execute(
					new PointChargeService.Command(userId, amount, System.currentTimeMillis()));
				countDownLatch.countDown();
			}

		));
	countDownLatch.await();

	// then
	UserPoint userPoint = userPointTable.selectById(userId);
	assertThat(userPoint.point()).isEqualTo(1_000L * threadCount);

	List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
	assertThat(pointHistories.size()).isEqualTo(threadCount);

}
```

위에 테스트 코드를 기반으로 동시성 문제를 확인했습니다.

### 1. synchronized 키워드

```java

@Service
@RequiredArgsConstructor
public class SynchronizedKeywordPointChargeService implements PointChargeService {
	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;

	@Override
	public synchronized UserPoint execute(Command command) {
		UserPoint chargedUserPoint = userPointTable.selectById(command.userId())
			.charge(command.amount(), command.currentTimeMillis());

		pointHistoryTable.insert(command.userId(), command.amount(), TransactionType.CHARGE,
			command.currentTimeMillis());
		return userPointTable.insertOrUpdate(command.userId(), chargedUserPoint.point());
	}
}

```

먼저, synchronized 키워드를 사용해 동시성 문제를 해결했습니다.
해당 방법은 정상적으로 동작했지만, 처리 건수가 늘어날수록 다음과 같이 실행 시간이 증가했습니다.

- 10건: 4.063s
- 100건: 41.069s
- 500건: 201s

### 2. synchronized block

synchronized 키워드를 사용한 방식은 임계 구역 외의 코드까지 잠금 범위에 포함되어, 실행 시간이 불필요하게 늘어나는 문제가 있었습니다.
이를 개선하기 위해 synchronized block을 사용하여 임계 구역 범위를 최소화했습니다.

```java

@Service
@RequiredArgsConstructor
public class SynchronizedPointChargeService implements PointChargeService {
	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;

	@Override
	public UserPoint execute(Command command) {
		UserPoint userPoint;
		synchronized (this.userPointTable) {
			UserPoint chargedUserPoint = userPointTable.selectById(command.userId())
				.charge(command.amount(), command.currentTimeMillis());

			userPoint = userPointTable.insertOrUpdate(command.userId(), chargedUserPoint.point());
		}
		pointHistoryTable.insert(command.userId(), command.amount(), TransactionType.CHARGE,
			command.currentTimeMillis());
		return userPoint;
	}
}
```

- 10건: 2.631s
- 100건: 25.504s
- 500건: 128s

| 처리 건수 | synchronized keyword | synchronized block | 개선 효과        |
|-------|----------------------|--------------------|--------------|
| 10건   | 4.063s               | 2.631s             | 약 **35%** 단축 |
| 100건  | 41.069s              | 25.504s            | 약 **38%** 단축 |
| 500건  | 201s                 | 128s               | 약 **36%** 단축 |

synchronized block은 임계 구역 범위를 최소화함으로써, 처리 건수가 증가할수록 약 35 ~ 38% 정도 성능 향상을 보였습니다.

### 3. ReentrantLock

동시성 이슈를 해결하기 위한 기법 중 Lock 인터페이스의 구현체인 ReentrantLock을 사용해 동시성 이슈 해결 및 실행 시간을 측정해 봤습니다.

```java

@Service
@RequiredArgsConstructor
public class ReentrantLockedPointChargeService implements PointChargeService {
	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;
	private final Map<Long, Lock> userIdToLockMap = new ConcurrentHashMap<>();

	@Override
	public UserPoint execute(Command command) {
		UserPoint userPoint;
		Lock lock = userIdToLockMap.computeIfAbsent(command.userId(), (userId) -> new ReentrantLock());
		lock.lock();
		try {
			UserPoint chargedUserPoint = userPointTable.selectById(command.userId())
				.charge(command.amount(), command.currentTimeMillis());
			userPoint = userPointTable.insertOrUpdate(command.userId(), chargedUserPoint.point());
		} finally {
			lock.unlock();
		}

		pointHistoryTable.insert(command.userId(), command.amount(), TransactionType.CHARGE,
			command.currentTimeMillis());
		return userPoint;

	}
}
```

- 10건: 2.333s
- 100건: 27.427s
- 500건: 130s

synchronized block과 성능적으로 큰 차이는 없었습니다.

# 동시성 제어 방식별 성능 비교 및 분석

## 성능 비교

| 처리 건수 | synchronized keyword | synchronized block | ReentrantLock |
|-------|----------------------|--------------------|---------------|
| 10건   | 4.063s               | 2.631s             | 2.333s        |
| 100건  | 41.069s              | 25.504s            | 27.427s       |
| 500건  | 201s                 | 128s               | 130s          |

- 세 방식 모두 동시성 문제는 정상적으로 해결할 수 있었습니다.
- **처리 건수가 증가할수록 잠금 범위가 성능에 많은 영향을 미친다**는 사실을 알 수 있었습니다.
  (synchronized keyword가 모든 구간에서 가장 긴 실행 시간을 기록)
- synchronized block 방식과 ReentrantLock을 비교했을 떄 성능적인 부분에선 차이가 없음을 알 수 있었습니다.

## 어떤 동시성 제어 방식을 사용할까?

### synchronized block 장/단점

- 장점
    - 구현이 간단하고, 가독성이 좋습니다.
    - 자동으로 잠금이 해제됩니다.
        - 개발자가 직접 잠금을 관리하지 않아도 되기 때문에 편리합니다.
    - ReentrantLock과 성능 차이가 거의 없습니다.
        - Java 6 이후부터 synchronized가 JIT 최적화를 받기 때문에 오버헤드가 크게 줄었습니다.
    - 디버깅하기 용이합니다.
        - JVM 자체적으로 synchronized의 모니터 락을 추적하기 때문에 상태 확인이 쉽습니다.
- 단점
    - 무한 대기 상태에 빠질 수 있습니다.
        - 잠금을 획득한 스레드 이외에 synchronized block에 접근한 스레드의 상태는 RUNNABLE에서 BLOCKED 상태가 됩니다.
        - BLOCKED 상태의 스레드는 잠금이 풀릴 때까지 무한 대기하게 되는데, 중간에 인터럽트나 특정 시간까지만 대기하는 타임 아웃이 존재하지 않습니다.
    - 공정성 문제가 있습니다.
        - 잠금이 풀렸을 때, BLOCKED 상태의 여러 스레드 중에 어떤 스레드가 락을 획득할지 알 수 없습니다.
        - 최악의 경우 특정 스레드는 너무 오랜시간 락을 획득하지 못할 수 있습니다.

### ReentrantLock 장/단점

- 장점
    - 공정성을 보장합니다.
        - 오래 기다린 스레드가 먼저 잠금을 획득할 수 있도록 보장 가능합니다.
    - 무한 대기 상태에 빠지지 않도록 타임 아웃을 설정할 수 있습니다.
- 단점
    - 잠금을 직접 관리해야 합니다.
        - 잠금을 획득했으면, 잠금을 해제하는 부분까지 추가적인 구현이 필요합니다.
    - 가독성 & 유지보수성 저하
        - 유지보수 시 잠금 범위를 잘못 수정하면 교착 상태나 락 누락 위험이 있습니다.

### 사용자 관점에서 본 동시성 제어 선택

TPS 300인 환경에서 사용자 요청을 처리한다고 가정했을 때,
가장 중요한 것은 사용자가 응답을 최대한 빨리 처리 받는 경험입니다.
synchronized를 사용할 경우 공정성을 보장할 수 없고, 타임아웃을 줄 수 없기 때문에 잠금을 획득할 때까지 무한정 대기할 수 있습니다.

코드의 간결성과 가독성, 성능만을 기준으로 synchronized block을 선택할 수 있습니다.
그러나 사용자 경험 측면에서 공정성과 타임아웃 기능을 고려했을 때는 ReentrantLock이 더 적합하다고 판단했습니다.
이에 따라 포인트 충전 및 사용 기능을 ReentrantLock을 사용하여 구현했습니다.
