package io.hhplus.tdd.point.application;

import io.hhplus.tdd.point.domain.UserPoint;

public interface PointChargeService {
	UserPoint execute(Command command);

	record Command(
		long userId,
		long amount,
		long currentTimeMillis
	) {

	}
}
