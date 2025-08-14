package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;

/**
 * synchronized block 사용하여 구현한 포인트 충전 서비스입니다.
 * 임계구역 범위를 최소화하여 구현했습니다.
 *
 */
@RequiredArgsConstructor
public class SynchronizedBlockPointChargeService implements PointChargeService {
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
