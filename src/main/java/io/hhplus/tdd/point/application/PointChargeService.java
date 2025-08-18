package io.hhplus.tdd.point.application;

import io.hhplus.tdd.point.domain.UserPoint;

/**
 * 다양한 방식으로 동시성 문제를 해결해보고자 PointChargeService를 추상화했습니다.
 * PointChargeService의 구현체는 아래와 같습니다.
 * - NonThreadSafePointChargeService
 * - SynchronizedKeywordPointChargeService
 * - SynchronizedBlockPointChargeService
 * - ReentrantLockedPointChargeService
 */
public interface PointChargeService {
	UserPoint execute(Command command);

	record Command(
		long userId,
		long amount,
		long currentTimeMillis
	) {

	}
}
