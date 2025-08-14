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
public class PointUseService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;
	private final Map<Long, Lock> userIdToLockMap = new ConcurrentHashMap<>();

	public UserPoint execute(Command command) {
		Lock lock = userIdToLockMap.computeIfAbsent(command.userId, (userId) -> new ReentrantLock(true));
		tryLock(lock);

		UserPoint userPoint;
		try {
			UserPoint usedUserPoint = userPointTable.selectById(command.userId)
				.use(command.amount, command.currentTimeMillis);
			userPoint = userPointTable.insertOrUpdate(usedUserPoint.id(), usedUserPoint.point());
		} finally {
			lock.unlock();
		}

		pointHistoryTable.insert(command.userId, command.amount, TransactionType.USE, command.currentTimeMillis);
		
		return userPoint;
	}

	private void tryLock(Lock lock) {
		try {
			if (!lock.tryLock(10, TimeUnit.SECONDS)) {
				throw new RuntimeException("잠시 후에 다시 시도해 주시기 바랍니다.");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public record Command(
		long userId,
		long amount,
		long currentTimeMillis
	) {
	}
}
