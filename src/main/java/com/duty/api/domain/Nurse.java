package com.duty.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nurse {
	private Integer nurseId;
	private String name;
	private Integer experience;
	private Integer isNightKeep;
}
