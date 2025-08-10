package io.hhplus.tdd.point.application;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointReadByIdService {
	private final UserPointTable userPointTable;

	public UserPoint read(long userId) {
		return userPointTable.selectById(userId);
	}
}
