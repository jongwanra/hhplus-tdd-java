package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.enums.TransactionType;

@ExtendWith(MockitoExtension.class)
class PointHistoryReadAllByUserIdServiceTest {
	@InjectMocks
	private PointHistoryReadAllByUserIdService pointHistoryReadAllByUserIdService;

	@Mock
	private PointHistoryTable pointHistoryTable;

	/**
	 * [작성 이유]
	 * 정상적으로 포인트 이용 내역이 id를 기준으로 내림차순으로 반환되는지 확인하고자 작성했습니다.
	 */
	@Test
	void 포인트_이용_내역이_id_내림차순으로_반환되는지_확인한다() {
		// given
		final long userId = 123L;
		final long timeMillisWhenCharge = System.currentTimeMillis();
		final long timeMillisWhenUse = timeMillisWhenCharge + 100_000L;
		given(pointHistoryTable.selectAllByUserId(userId))
			.willReturn(List.of(
				PointHistory
					.builder()
					.id(1L)
					.amount(1000L)
					.userId(userId)
					.type(TransactionType.CHARGE)
					.updateMillis(timeMillisWhenCharge)
					.build(),
				PointHistory.builder()
					.id(2L)
					.amount(1000L)
					.userId(userId)
					.type(TransactionType.USE)
					.updateMillis(timeMillisWhenUse)
					.build()
			));
		// when
		List<PointHistory> pointHistories = pointHistoryReadAllByUserIdService.read(userId);

		// then
		assertThat(pointHistories).hasSize(2);
		assertThat(pointHistories).extracting("id")
			.containsExactly(2L, 1L);
		assertThat(pointHistories).extracting("userId")
			.containsOnly(userId);
		assertThat(pointHistories).extracting("type")
			.containsExactly(TransactionType.USE, TransactionType.CHARGE);
		assertThat(pointHistories).extracting("amount")
			.containsOnly(1000L);
		assertThat(pointHistories).extracting("updateMillis")
			.containsExactly(timeMillisWhenUse, timeMillisWhenCharge);
	}

	/**
	 * [작성 이유]
	 * 포인트 이용 내역이 없을 경우 빈 리스트를 반환하는지 확인하고자 작성했습니다.
	 */
	@Test
	void 포인트_이용_내역이_없을_경우_빈_리스트를_반환한다() {
		// given
		final long userId = 123L;
		given(pointHistoryTable.selectAllByUserId(userId))
			.willReturn(List.of());
		// when
		List<PointHistory> pointHistories = pointHistoryReadAllByUserIdService.read(userId);

		// then
		assertThat(pointHistories).hasSize(0);
	}

}
