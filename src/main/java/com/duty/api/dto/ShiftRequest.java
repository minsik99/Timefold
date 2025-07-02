package com.duty.api.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ShiftRequest {
	private Integer reqId;
	private LocalDate reqDate;
	private String desiredShift;
}