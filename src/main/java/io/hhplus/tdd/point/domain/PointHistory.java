package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.domain.enums.TransactionType;
import lombok.Builder;

@Builder
public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
}
