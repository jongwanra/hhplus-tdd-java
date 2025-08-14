package io.hhplus.tdd.point.application;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;

/**
 * 사용자의 포인트를 조회합니다.
 */
@Service
@RequiredArgsConstructor
public class PointReadByIdService {
	private final UserPointTable userPointTable;

	public UserPoint read(long userId) {
		return userPointTable.selectById(userId);
	}
}
