package com.duty.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duty.api.dto.DutyRequestDTO;
import com.duty.api.dto.ScheduleDTO;
import com.duty.api.service.ScheduleService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	@CrossOrigin(origins = "http://localhost:9093")
	@PostMapping("/generate")
	public List<ScheduleDTO> generateSchedule(@RequestBody DutyRequestDTO dutyRequestDTO)
			throws JsonProcessingException {

		System.out.println(dutyRequestDTO);

		return scheduleService.generateSchedule(dutyRequestDTO);

	}
}