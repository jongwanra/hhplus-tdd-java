package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NonThreadSafePointChargeService implements PointChargeService {
	private final PointHistoryTable pointHistoryTable;
	private final UserPointTable userPointTable;

	@Override
	public UserPoint execute(Command command) {

		UserPoint userPoint = userPointTable.selectById(command.userId());
		log.info("스레드 {}: userPoint를 조회(현재 포인트:{})", Thread.currentThread().getId(), userPoint.point());
		UserPoint chargedUserPoint = userPoint.charge(command.amount(), command.currentTimeMillis());
		log.info("스레드 {}: 포인트를 충전(현재 포인트:{})", Thread.currentThread().getId(), chargedUserPoint.point());
		UserPoint savedUserPoint = userPointTable.insertOrUpdate(command.userId(), chargedUserPoint.point());
		log.info("스레드 {}: userPoint를 저장(현재 포인트:{})", Thread.currentThread().getId(), savedUserPoint.point());
		pointHistoryTable.insert(command.userId(), command.amount(), TransactionType.CHARGE,
			command.currentTimeMillis());
		return savedUserPoint;
	}

}
