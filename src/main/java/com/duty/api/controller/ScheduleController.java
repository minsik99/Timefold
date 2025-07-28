package com.duty.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duty.api.dto.DutyRequestDTO;
import com.duty.api.dto.ScheduleDTO;
import com.duty.api.service.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

//	@CrossOrigin(origins = "http://localhost:9093")
	@CrossOrigin(origins = "*")
	@PostMapping("/generate")
	public Map<String, Object> generateSchedule(@RequestBody Map<String, Object> param) {

		// request 데이터 추출 (형 변환)
		Map<String, Object> dmaData = (Map<String, Object>) param.get("dma_sample");
		System.out.println("dmaData : " + dmaData);

		// ObjectMapper 사용해서 java 객체로 변환
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		DutyRequestDTO dutyRequestDTO = mapper.convertValue(dmaData, DutyRequestDTO.class);

		// 자동 스케줄링 서비스 호출하여 데이터 생성
		List<ScheduleDTO> scheduleList = scheduleService.generateSchedule(dutyRequestDTO);

		// WebSquare 형태로 반환 (dataList로 변환)
		Map<String, Object> result = new HashMap<>();
		result.put("dlt_sample", scheduleList);

		return result;
	}
}