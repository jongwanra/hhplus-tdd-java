package io.hhplus.tdd.point.presentation.request;

import io.hhplus.tdd.point.application.PointUseService;

public record PointUseRequest(
	long amount
) {
	public PointUseService.Command toCommand(long userId) {
		return new PointUseService.Command(
			userId,
			amount,
			System.currentTimeMillis()
		);
	}
}
