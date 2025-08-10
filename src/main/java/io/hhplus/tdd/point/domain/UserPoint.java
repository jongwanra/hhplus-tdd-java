package io.hhplus.tdd.point.domain;

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

    private void validateCharge(long amount) {
        if(amount < MINIMUM_CHARGEABLE_POINT_PER_ONCE) {
            throw new ApplicationException("1 포인트 이상부터 충전이 가능합니다.");
        }
        if(amount > MAXIMUM_CHARGEABLE_POINT_PER_ONCE) {
            throw new ApplicationException("한 번에 충전할 수 있는 포인트를 초과했습니다.");
        }
        if(this.point + amount > MAXIMUM_HOLDABLE_POINT) {
            throw new ApplicationException("최대 가질 수 있는 포인트를 초과했습니다.");
        }
    }
}
