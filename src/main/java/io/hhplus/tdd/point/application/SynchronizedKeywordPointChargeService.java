package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;

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
