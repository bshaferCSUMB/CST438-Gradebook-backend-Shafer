package com.cst438.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.cst438.domain.CourseDTOG;

public class RegistrationServiceREST extends RegistrationService {

	
	RestTemplate restTemplate = new RestTemplate();
	
	@Value("${registration.url}") 
	String registration_url;
	
	public RegistrationServiceREST() {
		System.out.println("REST registration service ");
	}
	
	@Override
	public void sendFinalGrades(int course_id , CourseDTOG courseDTO) { 
		
		// Send a put request to the registration service with the courseDTO in the request body.
		System.out.println("REST Sending final grades: " + course_id + " " + courseDTO);
		restTemplate.put(registration_url + "/course/" + course_id, courseDTO);
		System.out.println("REST Done sending final grades.");

	} // end sendFinalGrades
} // RegistrationServiceREST
