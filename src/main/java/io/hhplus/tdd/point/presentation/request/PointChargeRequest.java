package io.hhplus.tdd.point.presentation.request;

import io.hhplus.tdd.point.application.NonThreadSafePointChargeService;

public record PointChargeRequest(
	long amount
) {
	public NonThreadSafePointChargeService.Command toCommand(long userId) {
		return new NonThreadSafePointChargeService.Command(
			userId,
			amount,
			System.currentTimeMillis()
		);
	}
}
