package io.hhplus.tdd.point.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.PointHistory;
import lombok.RequiredArgsConstructor;

/**
 * 포인트 충전/사용 내역 목록을 조회합니다.
 * PointHistory의 id값을 기준으로 내림차순 정렬했습니다.
 */

@Service
@RequiredArgsConstructor
public class PointHistoryReadAllByUserIdService {
	private final PointHistoryTable pointHistoryTable;

	public List<PointHistory> read(long userId) {
		return pointHistoryTable.selectAllByUserId(userId)
			.stream()
			.sorted(Comparator.comparingLong(PointHistory::id).reversed())
			.toList();
	}
}
