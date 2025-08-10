package io.hhplus.tdd.point.application;
import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.enums.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class PointChargeService {
	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;

	public UserPoint execute(Command command) {
		UserPoint userPoint = userPointTable.selectById(command.userId)
			.charge(command.amount, command.currentTimeMillis);

		pointHistoryTable.insert(command.userId, command.amount, TransactionType.CHARGE, command.currentTimeMillis);
		return userPointTable.insertOrUpdate(command.userId, userPoint.point());
	}

	public record Command(
		long userId,
		long amount,
		long currentTimeMillis
	){}
}
