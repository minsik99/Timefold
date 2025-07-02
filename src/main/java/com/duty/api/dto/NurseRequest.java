package com.duty.api.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class NurseRequest {
	private Integer nurseId;
	private String name;
	private Integer experience;
	private Integer isNightKeep;
	private List<ShiftRequest> requests;
}