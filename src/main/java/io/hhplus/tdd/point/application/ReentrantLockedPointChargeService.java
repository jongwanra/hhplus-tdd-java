package io.hhplus.tdd.point.application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;

/**
 * ReentrantLock을 사용하여 구현한 포인트 충전 서비스입니다.
 * 임계구역 범위를 최소화하여 구현했습니다.
 * 사용자 경험 측면해서 계속 대기하는 것 보다 실패하더라도 빠른 응답을 줄 수 있도록 락을 10초 동안 획득하지 못하면 실패를 응답하도록 구현했습니다.
 * 또한, 먼저 요청한 사용자가 먼저 응답받을 수 있도록 ReetrantLock의 공정성을 추가했습니다.
 */
@Service
@RequiredArgsConstructor
public class ReentrantLockedPointChargeService implements PointChargeService {
	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;
	private final Map<Long, Lock> userIdToLockMap = new ConcurrentHashMap<>();

	@Override
	public UserPoint execute(Command command) {
		UserPoint userPoint;
		Lock lock = userIdToLockMap.computeIfAbsent(command.userId(), (userId) -> new ReentrantLock(true));
		tryLock(lock);
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

	private void tryLock(Lock lock) {
		try {
			if (!lock.tryLock(10, TimeUnit.SECONDS)) {
				throw new RuntimeException("잠시 후에 다시 시도해 주시기 바랍니다. 다른 사용자가 포인트 충전 중입니다.");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
