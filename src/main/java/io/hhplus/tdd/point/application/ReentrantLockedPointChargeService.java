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
