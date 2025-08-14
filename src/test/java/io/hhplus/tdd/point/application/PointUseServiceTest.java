package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.global.exception.ApplicationException;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.domain.enums.TransactionType;

@ExtendWith(MockitoExtension.class)
class PointUseServiceTest {
	private PointUseService pointUseService;
	@Mock
	private PointHistoryTable pointHistoryTable;
	@Mock
	private UserPointTable userPointTable;

	@BeforeEach
	void setUp() {
		pointUseService = new ReentrantLockedPointUseService(userPointTable, pointHistoryTable);
	}

	/**
	 * [작성 이유]
	 * 사용자가 보유하고 있는 포인트를 정상적으로 사용하고, 사용한 내역을 확인하기 위해서 작성했습니다.
	 */
	@Test
	void 보유하고_있는_50_000_포인트를_정상적으로_전액_사용할_수_있다() {
		// given
		final long userId = 122L;
		final long amount = 50_000L;
		final long originPointBalance = 50_000L;
		final long newPointBalance = 0L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint
				.builder()
				.id(userId)
				.point(originPointBalance)
				.updateMillis(currentTimeMillis)
				.build());

		given(pointHistoryTable.insert(userId, amount, TransactionType.USE, currentTimeMillis))
			.willReturn(PointHistory
				.builder()
				.userId(userId)
				.amount(newPointBalance)
				.type(TransactionType.USE)
				.updateMillis(currentTimeMillis)
				.build());

		given(userPointTable.insertOrUpdate(userId, newPointBalance))
			.willReturn(
				UserPoint.builder()
					.id(userId)
					.point(newPointBalance)
					.updateMillis(currentTimeMillis)
					.build()
			);

		// when
		UserPoint userPoint = pointUseService.execute(
			new ReentrantLockedPointUseService.Command(userId, amount, currentTimeMillis));

		// then
		assertThat(userPoint.point()).isEqualTo(newPointBalance);
		assertThat(userPoint.id()).isEqualTo(userId);
		assertThat(userPoint.updateMillis()).isEqualTo(currentTimeMillis);

	}

	/**
	 * [작성 이유]
	 * 사용자가 보유하고 있는 포인트 보다 많은 포인트를 사용하려고 할 경우 예외를 발생시키는지 확인하기 위해서 작성했습니다.
	 */
	@Test
	void 보유하고_있는_포인트_보다_많은_포인트를_사용하려고_할_경우_예외를_발생시킨다() {
		// given
		final long userId = 122L;
		final long amountToUse = 50_001L;
		final long originPointBalance = 50_000L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint
				.builder()
				.id(userId)
				.point(originPointBalance)
				.updateMillis(currentTimeMillis)
				.build());

		// when & then
		assertThatThrownBy(() -> {
			pointUseService.execute(new ReentrantLockedPointUseService.Command(userId, amountToUse, currentTimeMillis));
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("보유하고 있는 포인트 보다 많은 포인트를 사용할 수 없습니다.");
	}

	/**
	 * [작성 이유]
	 * 사용자가 1 포인트 미만으로 사용하려고 할 경우 예외를 발생시키는지 확인하기 위해서 작성했습니다.
	 */
	@Test
	void 포인트를_1_포인트_미만으로_사용할_경우_예외를_발생시킨다() {
		// given
		final long userId = 122L;
		final long amountToUse = 0L;
		final long originPointBalance = 50_000L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint
				.builder()
				.id(userId)
				.point(originPointBalance)
				.updateMillis(currentTimeMillis)
				.build());

		// when & then
		assertThatThrownBy(() -> {
			pointUseService.execute(new ReentrantLockedPointUseService.Command(userId, amountToUse, currentTimeMillis));
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("1 포인트 미만으로 포인트를 사용할 수 없습니다.");
	}

	/**
	 * [작성 이유]
	 * 사용자가 100만 포인트를 초과하여 사용하려고 할 경우 예외를 발생시키는지 확인하기 위해서 작성했습니다.
	 */
	@Test
	void 사용자가_100만_포인트를_초과하여_사용하려고_할_경우_예외를_발생시킨다() {
		// given
		final long userId = 122L;
		final long amountToUse = 1_000_001L;
		final long originPointBalance = 5_000_000L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint
				.builder()
				.id(userId)
				.point(originPointBalance)
				.updateMillis(currentTimeMillis)
				.build());

		// when & then
		assertThatThrownBy(() -> {
			pointUseService.execute(new ReentrantLockedPointUseService.Command(userId, amountToUse, currentTimeMillis));
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("1,000,000 포인트를 초과하여 사용할 수 없습니다.");
	}

}
