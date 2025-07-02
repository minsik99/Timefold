package com.duty.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.duty.api.domain.Schedule;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;

@Configuration
public class TimefoldConfig {

	@Bean
	public Solver<Schedule> solver() {
		SolverFactory<Schedule> solverFactory = SolverFactory.createFromXmlResource("solverConfig.xml");
		return solverFactory.buildSolver();
	}
}
