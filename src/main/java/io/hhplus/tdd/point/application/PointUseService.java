package io.hhplus.tdd.point.application;

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

	public UserPoint execute(Command command) {
		UserPoint userPoint = userPointTable.selectById(command.userId)
			.use(command.amount, command.currentTimeMillis);

		pointHistoryTable.insert(command.userId, command.amount, TransactionType.USE, command.currentTimeMillis);
		return userPointTable.insertOrUpdate(userPoint.id(), userPoint.point());
	}

	public record Command(
		long userId,
		long amount,
		long currentTimeMillis
	) {}
}
