package com.group7.jobTrackerApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Job Application Tracker backend API.
 */
@SpringBootApplication
public class JobApplicationTrackerApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments supplied at startup
	 */
	public static void main(String[] args) {
		SpringApplication.run(JobApplicationTrackerApplication.class, args);
	}

}
