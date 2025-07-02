package com.duty.api.domain;

import java.time.LocalDate;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@PlanningEntity
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignment {

	@PlanningId
	private Integer id;

	private Nurse nurse;

	private LocalDate shiftDate;

	@PlanningVariable(valueRangeProviderRefs = { "shiftTypeRange" })
	private String shiftType; // D, E, N, Off

	private String desiredShift; // D, E, N, Off, null

	// === Getter/Setter ===

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Nurse getNurse() {
		return nurse;
	}

	public void setNurse(Nurse nurse) {
		this.nurse = nurse;
	}

	public LocalDate getShiftDate() {
		return shiftDate;
	}

	public void setShiftDate(LocalDate shiftDate) {
		this.shiftDate = shiftDate;
	}

	public String getShiftType() {
		return shiftType;
	}

	public void setShiftType(String shiftType) {
		this.shiftType = shiftType;
	}

	public String getDesiredShift() {
		return desiredShift;
	}

	public void setDesiredShift(String desiredShift) {
		this.desiredShift = desiredShift;
	}
}
