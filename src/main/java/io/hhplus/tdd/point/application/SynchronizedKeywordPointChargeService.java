package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;

/**
 * synchronized keyword를 사용하여 구현한 포인트 충전 서비스입니다.
 * 임계 구역 외의 코드까지 잠금 범위에 포함되어, 실행 시간이 불필요하게 늘어나는 문제가 있습니다.
 */
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
