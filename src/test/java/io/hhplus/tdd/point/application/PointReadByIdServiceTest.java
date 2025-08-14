package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;

@ExtendWith(MockitoExtension.class)
class PointReadByIdServiceTest {
	@InjectMocks
	private PointReadByIdService pointReadByIdService;

	@Mock
	private UserPointTable userPointTable;

	/**
	 * [작성 이유]
	 * 일반적으로 존재하는 사용자의 포인트를 조회할 수 있는지 확인하기 위해 작성했습니다.
	 */
	@Test
	void 존재하는_사용자의_포인트를_조회할_수_있다() {
		// given
		final long userId = 123L;
		final long updateMillis = System.currentTimeMillis();
		final long point = 10_000L;

		given(userPointTable.selectById(userId))
			.willReturn(UserPoint
				.builder()
				.id(userId)
				.point(point)
				.updateMillis(updateMillis)
				.build());

		// when
		UserPoint userPoint = pointReadByIdService.read(userId);
		// then
		assertThat(userPoint.id()).isEqualTo(userId);
		assertThat(userPoint.updateMillis()).isEqualTo(updateMillis);
		assertThat(userPoint.point()).isEqualTo(point);

	}

	/**
	 * [작성 이유]
	 * 존재하지 않는 사용자를 조회할 경우 기본값으로 반환하는지 확인하기 위해 작성했습니다.
	 */
	@Test
	void 존재하지_않는_사용자의_경우_기본값으로_제공한다() {
		// given
		final long notExistingUserId = 120L;

		given(userPointTable.selectById(notExistingUserId))
			.willReturn(UserPoint.empty(notExistingUserId));

		// when
		UserPoint userPoint = pointReadByIdService.read(notExistingUserId);
		
		// then
		assertThat(userPoint.id()).isEqualTo(notExistingUserId);
		assertThat(userPoint.point()).isEqualTo(0L);
		assertThat(userPoint.updateMillis()).isNotNull();

	}

}
