package io.hhplus.tdd.point.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.domain.PointHistory;
import lombok.RequiredArgsConstructor;

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
