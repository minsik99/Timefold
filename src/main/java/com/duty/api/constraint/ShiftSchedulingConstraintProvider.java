package com.duty.api.constraint;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.duty.api.domain.ShiftAssignment;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class ShiftSchedulingConstraintProvider implements ConstraintProvider {

	@Override
	public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
		return new Constraint[] {
				// 각 근무 타입별 정확히 5명씩 배정
				dailyShiftExactCount(constraintFactory, "D", 5, "주간 근무는 정확히 5명의 간호사가 필요합니다"),
				dailyShiftExactCount(constraintFactory, "E", 5, "저녁 근무는 정확히 5명의 간호사가 필요합니다"),
				dailyShiftExactCount(constraintFactory, "N", 5, "야간 근무는 정확히 5명의 간호사가 필요합니다"),
				maxConsecutiveNightShifts(constraintFactory), maxConsecutiveWorkDays(constraintFactory) };
	}

	/**
	 * 각 근무 타입별로 정확히 지정된 수의 간호사가 배정되도록 하는 제약
	 */
	private Constraint dailyShiftExactCount(ConstraintFactory factory, String shiftType, int exactCount, String name) {
		return factory.forEach(ShiftAssignment.class).filter(s -> shiftType.equals(s.getShiftType()))
				.groupBy(ShiftAssignment::getShiftDate, ConstraintCollectors.count())
				.filter((date, count) -> count != exactCount).penalize(HardSoftScore.ofHard(100), (date, count) -> {
					// 정확한 수가 아니면 차이만큼 페널티
					return Math.abs(count - exactCount) * 50;
				}).asConstraint(name);
	}

	private Constraint maxConsecutiveNightShifts(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).filter(s -> "N".equals(s.getShiftType()))
				.groupBy(s -> s.getNurse().getNurseId(), ConstraintCollectors.toList())
				.filter((id, shifts) -> hasConsecutiveDays(shifts, 3))
				.penalize(HardSoftScore.ofHard(20), (id, shifts) -> calculateConsecutivePenalty(shifts, 3))
				.asConstraint("최대 3일 연속 야간 근무");
	}

	private Constraint maxConsecutiveWorkDays(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).filter(s -> !"Off".equals(s.getShiftType()))
				.groupBy(s -> s.getNurse().getNurseId(), ConstraintCollectors.toList())
				.filter((id, shifts) -> hasConsecutiveDays(shifts, 4))
				.penalize(HardSoftScore.ofHard(15), (id, shifts) -> calculateConsecutivePenalty(shifts, 4))
				.asConstraint("최대 4일 연속 근무");
	}

	private boolean hasConsecutiveDays(List<ShiftAssignment> shifts, int maxDays) {
		List<LocalDate> dates = shifts.stream().map(ShiftAssignment::getShiftDate).distinct().sorted()
				.collect(Collectors.toList());
		int consecutive = 1;
		for (int i = 1; i < dates.size(); i++) {
			if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
				consecutive++;
				if (consecutive > maxDays)
					return true;
			} else {
				consecutive = 1;
			}
		}
		return false;
	}

	private int calculateConsecutivePenalty(List<ShiftAssignment> shifts, int maxDays) {
		List<LocalDate> dates = shifts.stream().map(ShiftAssignment::getShiftDate).distinct().sorted()
				.collect(Collectors.toList());
		int maxConsecutive = 1, currentConsecutive = 1;
		for (int i = 1; i < dates.size(); i++) {
			if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
				currentConsecutive++;
				maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
			} else {
				currentConsecutive = 1;
			}
		}
		return Math.max(0, maxConsecutive - maxDays);
	}
}