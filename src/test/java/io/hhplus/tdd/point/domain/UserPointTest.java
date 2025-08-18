package io.hhplus.tdd.point.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.hhplus.tdd.global.exception.ApplicationException;

class UserPointTest {

	@Test
	void 포인트_충전은_1_포인트_이상부터_가능하다() {
		// given
		final UserPoint userPoint = UserPoint.empty(1L);

		// when
		final UserPoint chargedUserPoint = userPoint.charge(1L, System.currentTimeMillis());

		// then
		assertThat(chargedUserPoint.point()).isEqualTo(1L);
	}

	@Test
	void 포인트_충전을_1_포인트_미만으로_할_경우_예외를_발생_시킨다() {
		// given
		final UserPoint userPoint = UserPoint.empty(1L);

		// when & then
		assertThatThrownBy(() -> {
			userPoint.charge(0L, System.currentTimeMillis());
		})
			.hasMessage("1 포인트 이상부터 충전이 가능합니다.")
			.isInstanceOf(ApplicationException.class);

	}

	@Test
	void 포인트_충전은_한_번에_100만_포인트를_초과하여_충전할_경우_예외를_발생시킨다() {
		// given
		final UserPoint userPoint = UserPoint.empty(1L);

		// when & then
		assertThatThrownBy(() -> {
			userPoint.charge(1_000_001L, System.currentTimeMillis());
		})
			.hasMessage("한 번에 충전할 수 있는 포인트를 초과했습니다.")
			.isInstanceOf(ApplicationException.class);

	}

	@Test
	void 포인트_충전은_한_번에_100만_포인트_까지_충전할_수_있다() {
		// given
		final UserPoint userPoint = UserPoint.empty(1L);

		// when
		UserPoint chargedUserPoint = userPoint.charge(1_000_000L, System.currentTimeMillis());

		// then
		assertThat(chargedUserPoint.point()).isEqualTo(1_000_000L);

	}

	@Test
	void 포인트는_최대_1000만_포인트까지_보유할_수_있다() {
		// given
		UserPoint userPoint = UserPoint.empty(1L);

		// when
		for (int index = 0; index < 10; index++) {
			userPoint = userPoint.charge(1_000_000L, System.currentTimeMillis());
		}

		// then
		assertThat(userPoint.point()).isEqualTo(10_000_000L);

	}

	@Test
	void 포인트는_최대_1000만_포인트를_넘어서_충전하려고_할_경우_예외를_발생시킨다() {
		// given
		final UserPoint userPoint = UserPoint.builder()
			.point(10_000_000L)
			.build();

		// when & then
		assertThatThrownBy(() -> userPoint.charge(1L, System.currentTimeMillis()))
			.isInstanceOf(ApplicationException.class)
			.hasMessage("최대 가질 수 있는 포인트를 초과했습니다.");

	}

	@Test
	void 한_번에_100만_포인트_까지_사용할_수_있다() {
		// given
		UserPoint userPoint = UserPoint.builder()
			.point(1_000_000L)
			.build();

		// when
		UserPoint usedUserPoint = userPoint.use(1_000_000L, System.currentTimeMillis());

		// given
		assertThat(usedUserPoint.point()).isEqualTo(0L);
	}

	@Test
	void 한_번에_100만_포인트_이상_사용할_경우_예외를_발생시킨다() {
		// given
		UserPoint userPoint = UserPoint.builder()
			.point(1_000_001L)
			.build();

		// when & then
		assertThatThrownBy(() -> {
			userPoint.use(1_000_001L, System.currentTimeMillis());
		})
			.isInstanceOf(ApplicationException.class)
			.hasMessage("1,000,000 포인트를 초과하여 사용할 수 없습니다.");

	}

	@Test
	void 보유하고_있는_포인트_액수_만큼_사용할_수_있다() {
		// given
		UserPoint userPoint = UserPoint.builder()
			.point(1000L)
			.build();

		// when
		UserPoint usedUserPoint = userPoint.use(1_000L, System.currentTimeMillis());

		// then
		assertThat(usedUserPoint.point()).isZero();
	}

	@Test
	void 보유하고_있는_포인트_보다_더_사용할_경우_예외를_발생시킨다() {
		// given
		UserPoint userPoint = UserPoint.builder()
			.point(1000L)
			.build();

		// when & then
		assertThatThrownBy(() -> userPoint.use(1_001L, System.currentTimeMillis()))
			.hasMessage("보유하고 있는 포인트 보다 많은 포인트를 사용할 수 없습니다.")
			.isInstanceOf(ApplicationException.class);

	}

	@Test
	void 최소_1_포인트_이상_사용할_수_있다() {
		// given
		UserPoint userPoint = UserPoint.builder()
			.point(1_000L)
			.build();

		// when
		UserPoint usedUserPoint = userPoint.use(1L, System.currentTimeMillis());

		// then
		assertThat(usedUserPoint.point()).isEqualTo(999L);

	}

	@Test
	void 포인트_1_미만으로_포인트를_사용하려_할_경우_예외를_발생시킨다() {
		// given
		UserPoint userPoint = UserPoint.builder()
			.point(1_000L)
			.build();

		// when & then
		assertThatThrownBy(() -> userPoint.use(0L, System.currentTimeMillis()))
			.hasMessage("1 포인트 미만으로 포인트를 사용할 수 없습니다.")
			.isInstanceOf(ApplicationException.class);

	}

}
