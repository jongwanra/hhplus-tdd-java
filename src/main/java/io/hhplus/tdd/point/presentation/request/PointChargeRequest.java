package io.hhplus.tdd.point.presentation.request;

import io.hhplus.tdd.point.application.PointChargeService;

public record PointChargeRequest(
	long amount
) {
	public PointChargeService.Command toCommand(long userId) {
		return new PointChargeService.Command(
			userId,
			amount,
			System.currentTimeMillis()
		);
	}
}
