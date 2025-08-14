package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;

class PointUseServiceIntegrationTest {
	private PointUseService pointUseService;
	private UserPointTable userPointTable;
	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		pointHistoryTable = new PointHistoryTable();
		userPointTable = new UserPointTable();
		pointUseService = new ReentrantLockedPointUseService(userPointTable, pointHistoryTable);
	}

	/**
	 * [작성 이유]
	 * 동일한 회원이 동시에 10번 포인트를 사용하려고 했을 때, 동시성 이슈 발생 여부를 확인하고자 작성했습니다.
	 */

	@Test
	void 회원이_가지고_있는_10000_포인트를_1000_포인트씩_동시에_10번_사용할_경우_최종적으로_포인트는_0원이_된다() throws InterruptedException {
		// given
		final int threadCount = 10;
		final long userId = 123L;
		final long amount = 1000L;
		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		userPointTable.insertOrUpdate(userId, threadCount * 1_000L);

		// when
		IntStream.range(0, threadCount)
			.forEach((index) -> executorService.execute(() -> {
				pointUseService.execute(new ReentrantLockedPointUseService.Command(
					userId, amount, System.currentTimeMillis()
				));
				countDownLatch.countDown();
			}));

		countDownLatch.await();

		// then
		UserPoint userPoint = userPointTable.selectById(userId);
		assertThat(userPoint.point()).isZero();

		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
		assertThat(pointHistories.size()).isEqualTo(threadCount);
	}

	/**
	 * [작성 이유]
	 * 각자 다른 회원들이 동시에 접근했을 때 동시성 이슈가 발생하는지 여부를 확인하고자 작성했습니다.
	 */

	@Test
	void 회원_10명이_동시에_자신이_가지고_있는_10000_포인트_중_5000_포인트를_사용할_경우_각각_5000_포인트씩_남게_된다() throws InterruptedException {
		// given
		final int threadCount = 10;
		final long amountToCharge = 10_000L;
		final long amountToUse = 5_000L;
		final long pointBalance = amountToCharge - amountToUse;

		for (long userId = 1; userId <= threadCount; userId++) {
			// user 마다 10,000 포인트씩 충전한다.
			userPointTable.insertOrUpdate(userId, amountToCharge);
		}

		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		// when
		IntStream.range(1, threadCount + 1)
			.forEach((userId) -> executorService.execute(() -> {
				// user 마다 5,000 포인트씩 사용한다.
				pointUseService.execute(new ReentrantLockedPointUseService.Command(
					userId, amountToUse, System.currentTimeMillis()
				));
				countDownLatch.countDown();
			}));

		countDownLatch.await();

		// then
		for (long userId = 1; userId <= threadCount; userId++) {
			UserPoint userPoint = userPointTable.selectById(userId);
			assertThat(userPoint.point()).isEqualTo(pointBalance);
		}

	}
}
