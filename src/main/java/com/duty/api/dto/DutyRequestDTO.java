package com.duty.api.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class DutyRequestDTO {
	private Integer maxNurses;
	private LocalDate startDate;
	private LocalDate endDate;
	private List<NurseRequest> nurseList;
}
