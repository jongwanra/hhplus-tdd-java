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
public class PointChargeServiceTest {
	private PointChargeService pointChargeService;
	@Mock
	private PointHistoryTable pointHistoryTable;
	@Mock
	private UserPointTable userPointTable;

	@BeforeEach
	void setUp() {
		pointChargeService = new ReentrantLockedPointChargeService(pointHistoryTable, userPointTable);
	}

	/**
	 * [작성 이유]
	 * 정상적으로 사용자 50,000 포인트 충전 및 포인트 내역에 기록되는지 확인하기 위해 작성했습니다.
	 */
	@Test
	void 사용자는_50_000_포인트를_충전할_수_있다() {
		// given
		final long userId = 2323L;
		final long amount = 50_000L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint.empty(userId));

		given(userPointTable.insertOrUpdate(userId, amount))
			.willReturn(UserPoint.builder()
				.id(userId)
				.updateMillis(currentTimeMillis)
				.point(50_000)
				.build());

		given(pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, currentTimeMillis))
			.willReturn(PointHistory.builder()
				.amount(amount)
				.id(1L)
				.type(TransactionType.CHARGE)
				.updateMillis(currentTimeMillis)
				.userId(userId)
				.build());

		// when
		PointChargeService.Command command = new PointChargeService.Command(userId, amount,
			currentTimeMillis);
		UserPoint userPoint = pointChargeService.execute(command);

		// then
		assertThat(userPoint.id()).isEqualTo(userId);
		assertThat(userPoint.point()).isEqualTo(amount);
		assertThat(userPoint.updateMillis()).isEqualTo(currentTimeMillis);
	}

	/**
	 * [작성 이유]
	 * 사용자의 포인트를 100만 포인트를 초과하여 충전할 경우 예외가 발생하는지 확인해보기 위해 작성했습니다.
	 */
	@Test
	void 사용자의_포인트는_한_번에_100만_포인트까지_충전_가능하다() {
		// given
		final long userId = 2323L;
		final long amount = 1_000_001L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint.empty(userId));

		PointChargeService.Command command = new PointChargeService.Command(userId, amount,
			currentTimeMillis);

		// when & then
		assertThatThrownBy(() -> {
			pointChargeService.execute(command);
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("한 번에 충전할 수 있는 포인트를 초과했습니다.");
	}

	/**
	 * [작성 이유]
	 * 사용자가 최대 가질 수 있는 포인트를 초과하여 충전할 경우, 예외가 발생하는지 확인하려고 작성했습니다.
	 */
	@Test
	void 사용자는_최대_1000만_포인트까지_가질_수_있다() {
		// given
		final long userId = 122L;
		final long amount = 1L;
		final long currentTimeMillis = System.currentTimeMillis();
		final long currentPoint = 10_000_000L;

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint
				.builder()
				.id(userId)
				.updateMillis(currentTimeMillis)
				.point(currentPoint)
				.build());

		PointChargeService.Command command = new PointChargeService.Command(userId, amount,
			currentTimeMillis);

		// when & then
		assertThatThrownBy(() -> {
			pointChargeService.execute(command);
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("최대 가질 수 있는 포인트를 초과했습니다.");
	}

	/**
	 * [작성 이유]
	 * 충전하고자 하는 포인트가 1 포인트 미만일 경우 예외가 발생하는지 확인하기 위해 작성했습니다.
	 */
	@Test
	void 포인트는_1_포인트_이상부터_충전이_가능하다() {
		final long userId = 122L;
		final long amount = 0L;
		final long currentTimeMillis = System.currentTimeMillis();

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint.empty(122L));

		NonThreadSafePointChargeService.Command command = new NonThreadSafePointChargeService.Command(userId, amount,
			currentTimeMillis);

		// when & then
		assertThatThrownBy(() -> {
			pointChargeService.execute(command);
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("1 포인트 이상부터 충전이 가능합니다.");
	}

}
