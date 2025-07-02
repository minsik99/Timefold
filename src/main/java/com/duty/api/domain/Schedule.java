package com.duty.api.domain;

import java.time.LocalDate;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.NoArgsConstructor;

@PlanningSolution
@NoArgsConstructor
public class Schedule {

	// 최적화 대상
	@PlanningEntityCollectionProperty
	private List<ShiftAssignment> shiftAssignments;

	// 점수
	@PlanningScore
	private HardSoftScore score;

	// 간호사 정보
	@ValueRangeProvider(id = "nurseRange")
	@ProblemFactCollectionProperty
	private List<Nurse> nurseList;

	// 날짜 정보
	@ValueRangeProvider(id = "dateRange")
	@ProblemFactCollectionProperty
	private List<LocalDate> shiftDateList;

	@ValueRangeProvider(id = "shiftTypeRange")
	@ProblemFactCollectionProperty
	private List<String> shiftTypeRange = List.of("D", "E", "N", "Off");

	public Schedule(List<ShiftAssignment> shiftAssignments, List<Nurse> nurseList, List<LocalDate> shiftDateList) {
		this.shiftAssignments = shiftAssignments;
		this.nurseList = nurseList;
		this.shiftDateList = shiftDateList;
		this.shiftTypeRange = List.of("D", "E", "N", "Off");
	}

	public List<ShiftAssignment> getShiftAssignments() {
		return shiftAssignments;
	}

	public void setShiftAssignments(List<ShiftAssignment> shiftAssignments) {
		this.shiftAssignments = shiftAssignments;
	}

	public HardSoftScore getScore() {
		return score;
	}

	public void setScore(HardSoftScore score) {
		this.score = score;
	}

	public List<Nurse> getNurseList() {
		return nurseList;
	}

	public void setNurseList(List<Nurse> nurseList) {
		this.nurseList = nurseList;
	}

	public List<LocalDate> getShiftDateList() {
		return shiftDateList;
	}

	public void setShiftDateList(List<LocalDate> shiftDateList) {
		this.shiftDateList = shiftDateList;
	}
}
