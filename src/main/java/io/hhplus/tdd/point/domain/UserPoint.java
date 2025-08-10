package io.hhplus.tdd.point.domain;

import java.text.NumberFormat;

import io.hhplus.tdd.global.exception.ApplicationException;
import lombok.Builder;

@Builder
public record UserPoint(
	long id,
	long point,
	long updateMillis
) {

	private static final int MAXIMUM_HOLDABLE_POINT = 10_000_000;
	private static final int MAXIMUM_CHARGEABLE_POINT_PER_ONCE = 1_000_000;
	private static final int MINIMUM_CHARGEABLE_POINT_PER_ONCE = 1;
	private static final int MINIMUM_USABLE_POINT = 1;
	private static final long MAXIMUM_USABLE_POINT = 1_000_000L;

	public static UserPoint empty(long id) {
		return new UserPoint(id, 0, System.currentTimeMillis());
	}

	public UserPoint charge(long amount, long updateMillis) {
		validateCharge(amount);
		return UserPoint
			.builder()
			.id(this.id)
			.point(this.point + amount)
			.updateMillis(updateMillis)
			.build();
	}

	public UserPoint use(long amount, long updateMillis) {
		validateUse(amount);
		final long newPointBalance = this.point - amount;
		return UserPoint.builder()
			.id(this.id)
			.point(newPointBalance)
			.updateMillis(updateMillis)
			.build();
	}

	private void validateUse(long amount) {
		if (amount > MAXIMUM_USABLE_POINT) {
			final String formattedMaximumUsablePoint = NumberFormat.getNumberInstance().format(MAXIMUM_USABLE_POINT);
			throw new ApplicationException(formattedMaximumUsablePoint + " 포인트를 초과하여 사용할 수 없습니다.");
		}
		if (amount < MINIMUM_USABLE_POINT) {
			throw new ApplicationException("1 포인트 미만으로 포인트를 사용할 수 없습니다.");
		}

		final long newPointBalance = this.point - amount;
		if (newPointBalance < 0) {
			throw new ApplicationException("보유하고 있는 포인트 보다 많은 포인트를 사용할 수 없습니다.");
		}
	}

	private void validateCharge(long amount) {
		if (amount < MINIMUM_CHARGEABLE_POINT_PER_ONCE) {
			throw new ApplicationException("1 포인트 이상부터 충전이 가능합니다.");
		}
		if (amount > MAXIMUM_CHARGEABLE_POINT_PER_ONCE) {
			throw new ApplicationException("한 번에 충전할 수 있는 포인트를 초과했습니다.");
		}
		if (this.point + amount > MAXIMUM_HOLDABLE_POINT) {
			throw new ApplicationException("최대 가질 수 있는 포인트를 초과했습니다.");
		}
	}

}
