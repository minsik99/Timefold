package com.duty.api.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ScheduleDTO {
	private Integer schId;
	private LocalDate shiftDate;
	private Integer nurseId;
	private String shiftType;
}