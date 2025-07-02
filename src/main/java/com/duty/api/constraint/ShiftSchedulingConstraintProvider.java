package com.duty.api.constraint;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.duty.api.domain.ShiftAssignment;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

public class ShiftSchedulingConstraintProvider implements ConstraintProvider {

	@Override
	public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
		return new Constraint[] { dailyShiftRangeCount(constraintFactory, "D", 4, 6, "주간 근무는 4-6명의 간호사가 필요합니다"),
				dailyShiftRangeCount(constraintFactory, "E", 4, 6, "저녁 근무는 4-6명의 간호사가 필요합니다"),
				dailyShiftRangeCount(constraintFactory, "N", 4, 6, "야간 근무는 4-6명의 간호사가 필요합니다"),
				nightKeepAssignment(constraintFactory), chargeNurseRequirement(constraintFactory),
				maxConsecutiveNightShifts(constraintFactory), maxConsecutiveWorkDays(constraintFactory),
				forbiddenShiftPatterns(constraintFactory), maxNewNursesPerDuty(constraintFactory),
				nightRestPeriod(constraintFactory), preferredShift(constraintFactory), fairOffDays(constraintFactory),
				preferRegularWorkBlocks(constraintFactory), preferRealisticPatterns(constraintFactory) };
	}

	private Constraint preferRealisticPatterns(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).groupBy(ShiftAssignment::getNurse, ConstraintCollectors.toList())
				.reward(HardSoftScore.ofSoft(100), (nurse, shifts) -> {
					List<String> pattern = shifts.stream().sorted(Comparator.comparing(ShiftAssignment::getShiftDate))
							.map(ShiftAssignment::getShiftType).collect(Collectors.toList());

					int score = 0;
					for (int i = 0; i < pattern.size() - 2; i++) {
						String p1 = pattern.get(i), p2 = pattern.get(i + 1), p3 = pattern.get(i + 2);
						if (p1.equals(p2) && p3.equals("Off")) {
							score++;
						} else if (p1.equals(p2) && p2.equals(p3)) {
							score += 2; // DDD, EEE, NNN 등
						}
					}
					return score;
				}).asConstraint("유연한 실무 근무 패턴 보상");
	}

	private Constraint dailyShiftRangeCount(ConstraintFactory factory, String shiftType, int minCount, int maxCount,
			String name) {
		return factory.forEach(ShiftAssignment.class).filter(s -> shiftType.equals(s.getShiftType()))
				.groupBy(ShiftAssignment::getShiftDate, ConstraintCollectors.count())
				.filter((date, count) -> count < minCount || count > maxCount)
				.penalize(HardSoftScore.ofHard(8), (date, count) -> {
					if (count < minCount)
						return (minCount - count) * 8;
					return (count - maxCount) * 8;
				}).asConstraint(name);
	}

	private Constraint preferRegularWorkBlocks(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).groupBy(ShiftAssignment::getNurse, ConstraintCollectors.toList())
				.penalize(HardSoftScore.ofSoft(20), (nurse, shifts) -> {
					List<LocalDate> dates = shifts.stream().filter(s -> !"Off".equals(s.getShiftType()))
							.map(ShiftAssignment::getShiftDate).distinct().sorted().collect(Collectors.toList());

					int score = 0;
					int block = 1;
					for (int i = 1; i < dates.size(); i++) {
						if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
							block++;
						} else {
							if (block == 1) {
								score += 2;
							} else if (block > 4) {
								score += (block - 4) * 3;
							}
							block = 1;
						}
					}
					return score;
				}).asConstraint("선호되는 3~4일 근무 블록 유지");
	}

	private Constraint chargeNurseRequirement(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).filter(s -> !"Off".equals(s.getShiftType()))
				.groupBy(ShiftAssignment::getShiftDate, ShiftAssignment::getShiftType, ConstraintCollectors.count(),
						ConstraintCollectors.countDistinct(s -> s.getNurse().getExperience() >= 36))
				.filter((date, shiftType, totalCount, chargeCount) -> totalCount >= 4 && chargeCount < 1)
				.penalize(HardSoftScore.ofHard(10)).asConstraint("각 근무(D, E, N)에 최소 1명의 책임 간호사가 필요합니다");
	}

	private Constraint fairOffDays(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).filter(s -> "Off".equals(s.getShiftType()))
				.groupBy(s -> s.getNurse().getNurseId(), ConstraintCollectors.count())
				.filter((nurseId, offCount) -> offCount < 10 || offCount > 11)
				.penalize(HardSoftScore.ofHard(10), (nurseId, offCount) -> {
					if (offCount < 10)
						return (10 - offCount) * 10;
					return (offCount - 11) * 10;
				}).asConstraint("월별 휴일 10~11일 유지");
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

	private Constraint forbiddenShiftPatterns(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class)
				.join(ShiftAssignment.class, Joiners.equal(a -> a.getNurse().getNurseId()),
						Joiners.filtering((a, b) -> a.getShiftDate().plusDays(1).equals(b.getShiftDate())))
				.filter((a, b) -> (a.getShiftType().equals("N") && b.getShiftType().equals("D"))
						|| (a.getShiftType().equals("E") && b.getShiftType().equals("D")))
				.penalize(HardSoftScore.ofHard(10)).asConstraint("금지된 근무 패턴 (N->D 또는 E->D 다음 날)");
	}

	private Constraint maxNewNursesPerDuty(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class)
				.filter(s -> !"Off".equals(s.getShiftType()) && s.getNurse().getExperience() < 6)
				.groupBy(ShiftAssignment::getShiftDate, ConstraintCollectors.count()).filter((date, count) -> count > 2)
				.penalize(HardSoftScore.ofHard(10), (date, count) -> (count - 2) * 5).asConstraint("하루 최대 2명의 신규 간호사");
	}

	private Constraint nightRestPeriod(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class).filter(a -> "N".equals(a.getShiftType()))
				.join(ShiftAssignment.class, Joiners.equal(a -> a.getNurse().getNurseId()),
						Joiners.filtering((a, b) -> b.getShiftDate().isAfter(a.getShiftDate())
								&& b.getShiftDate().isBefore(a.getShiftDate().plusDays(3))))
				.filter((a, b) -> !"Off".equals(b.getShiftType())).penalize(HardSoftScore.ofHard(20))
				.asConstraint("야간 근무 후 48시간 휴식");
	}

	private Constraint nightKeepAssignment(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class)
				.filter(s -> s.getNurse().getIsNightKeep() == 1 && !"N".equals(s.getShiftType()))
				.penalize(HardSoftScore.ofHard(100)).asConstraint("야간 전담 간호사는 야간 근무만 배정");
	}

	private Constraint preferredShift(ConstraintFactory factory) {
		return factory.forEach(ShiftAssignment.class)
				.filter(s -> s.getDesiredShift() != null && !s.getDesiredShift().equals(s.getShiftType()))
				.penalize(HardSoftScore.ofSoft(3)).asConstraint("선호 근무 미충족");
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