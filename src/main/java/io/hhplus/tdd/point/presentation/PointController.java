package io.hhplus.tdd.point.presentation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.tdd.point.application.PointChargeService;
import io.hhplus.tdd.point.application.PointUseService;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.presentation.request.PointChargeRequest;
import io.hhplus.tdd.point.presentation.request.PointUseRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {
	private static final Logger log = LoggerFactory.getLogger(PointController.class);
	private final PointChargeService pointChargeService;
	private final PointUseService pointUseService;

	/**
	 * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
	 */
	@GetMapping("{id}")
	public UserPoint point(
		@PathVariable("id") long id
	) {
		return new UserPoint(0, 0, 0);
	}

	/**
	 * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
	 */
	@GetMapping("{id}/histories")
	public List<PointHistory> history(
		@PathVariable long id
	) {
		return List.of();
	}

	/**
	 * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
	 */
	@PatchMapping("{id}/charge")
	@ResponseStatus(HttpStatus.OK)
	public UserPoint charge(
		@PathVariable("id") long id,
		@RequestBody PointChargeRequest request
	) {
		return pointChargeService.execute(request.toCommand(id));
	}

	/**
	 * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
	 */
	@PatchMapping("{id}/use")
	@ResponseStatus(HttpStatus.OK)
	public UserPoint use(
		@PathVariable("id") long id,
		@RequestBody PointUseRequest request
	) {
		return pointUseService.execute(request.toCommand(id));
	}
}
