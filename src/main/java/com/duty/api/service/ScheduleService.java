package com.duty.api.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.duty.api.domain.Nurse;
import com.duty.api.domain.Schedule;
import com.duty.api.domain.ShiftAssignment;
import com.duty.api.dto.DutyRequestDTO;
import com.duty.api.dto.NurseRequest;
import com.duty.api.dto.ScheduleDTO;
import com.duty.api.dto.ShiftRequest;

import ai.timefold.solver.core.api.solver.Solver;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final Solver<Schedule> solver;

	public List<ScheduleDTO> generateSchedule(DutyRequestDTO dutyRequestDTO) {
		List<NurseRequest> nurseRequests = dutyRequestDTO.getNurseList();
		LocalDate startDate = dutyRequestDTO.getStartDate();
		LocalDate endDate = dutyRequestDTO.getEndDate();

		// 날짜 범위 생성
		List<LocalDate> shiftDateList = new ArrayList<>();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			shiftDateList.add(date);
		}

		// ShiftAssignment 생성
		int idCounter = 1;
		List<Nurse> nurseList = new ArrayList<>();
		List<ShiftAssignment> assignmentList = new ArrayList<>();

		// 기본적으로 D, E, N 근무 배정
		for (NurseRequest nurseReq : nurseRequests) {
			Nurse nurse = new Nurse(nurseReq.getNurseId(), nurseReq.getName(), nurseReq.getExperience(),
					nurseReq.getIsNightKeep());
			nurseList.add(nurse);

			for (LocalDate date : shiftDateList) {
				// 신청이 없을 때는 기본적으로 배치 (D, E, N)
				String desiredShift = nurseReq.getRequests().stream().filter(req -> req.getReqDate().equals(date))
						.map(ShiftRequest::getDesiredShift).findFirst().orElse(null);

				// 신청이 없으면 기본적으로 D, E, N을 AI가 배정
				String shiftType = desiredShift != null ? desiredShift : getDefaultShiftForNurse(nurse);

				assignmentList.add(new ShiftAssignment(idCounter++, nurse, date, shiftType, desiredShift));
			}
		}

		// 후처리: Off 배정 (빈 자리에 Off 배정)
		for (ShiftAssignment assignment : assignmentList) {
			if (assignment.getShiftType() == null) {
				assignment.setShiftType("Off"); // 빈 자리에 Off 배정
			}
		}

		// Schedule 구성
		Schedule schedule = new Schedule(assignmentList, nurseList, shiftDateList);

		// Solver로 최적화
		Schedule solvedSchedule = solver.solve(schedule);

		// 결과 변환 및 후처리: Off 배정
		return solvedSchedule.getShiftAssignments().stream().map(assign -> {
			ScheduleDTO dto = new ScheduleDTO();
			dto.setSchId(assign.getId());
			dto.setShiftDate(assign.getShiftDate());
			dto.setNurseId(assign.getNurse().getNurseId());
			dto.setShiftType(assign.getShiftType()); // Off는 후처리로 자동 배정됨
			return dto;
		}).collect(Collectors.toList());
	}

	// 기본적으로 배치할 근무 형태를 결정하는 메서드
	private String getDefaultShiftForNurse(Nurse nurse) {
		if (nurse.getIsNightKeep() == 1) {
			return "N"; // 나이트킵 간호사는 반드시 N 배정
		} else {
			return "D"; // 나머지 간호사는 기본적으로 Day shift로 배정
		}
	}
}
