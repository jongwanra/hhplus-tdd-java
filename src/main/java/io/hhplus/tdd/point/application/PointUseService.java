package io.hhplus.tdd.point.application;

import io.hhplus.tdd.point.domain.UserPoint;

public interface PointUseService {
	UserPoint execute(ReentrantLockedPointUseService.Command command);

	record Command(
		long userId,
		long amount,
		long currentTimeMillis
	) {
	}
}
