package io.hhplus.tdd.point.presentation.request;

import io.hhplus.tdd.point.application.ReentrantLockedPointUseService;

public record PointUseRequest(
	long amount
) {
	public ReentrantLockedPointUseService.Command toCommand(long userId) {
		return new ReentrantLockedPointUseService.Command(
			userId,
			amount,
			System.currentTimeMillis()
		);
	}
}
