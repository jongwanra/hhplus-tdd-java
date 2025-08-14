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

public class PointChargeServiceIntegrationTest {
	private PointChargeService pointChargeService;
	private UserPointTable userPointTable;
	private PointHistoryTable pointHistoryTable;

	@BeforeEach
	void setUp() {
		userPointTable = new UserPointTable();
		pointHistoryTable = new PointHistoryTable();
		// pointChargeService = new NonThreadSafePointChargeService(pointHistoryTable, userPointTable);
		// pointChargeService = new SynchronizedPointChargeService(pointHistoryTable, userPointTable);
		// pointChargeService = new SynchronizedKeywordPointChargeService(pointHistoryTable, userPointTable);
		pointChargeService = new ReentrantLockedPointChargeService(pointHistoryTable, userPointTable);
	}

	@Test
	void 동일한_회원이_동시에_10번_1000_포인트_충전을_요청할_경우_10_000원을_최종적으로_반환한다() throws InterruptedException {
		// given
		final int threadCount = 10;
		final long userId = 123L;
		final long amount = 1000L;

		CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		// when
		IntStream.range(0, threadCount)
			.forEach((index) -> executorService.execute(() -> {
					pointChargeService.execute(
						new PointChargeService.Command(userId, amount, System.currentTimeMillis()));
					countDownLatch.countDown();
				}

			));
		countDownLatch.await();

		// then
		UserPoint userPoint = userPointTable.selectById(userId);
		assertThat(userPoint.point()).isEqualTo(1_000L * threadCount);

		List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(userId);
		assertThat(pointHistories.size()).isEqualTo(threadCount);

	}
}
