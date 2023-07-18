package com.cst438.services;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;


public class RegistrationServiceMQ extends RegistrationService {

	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Autowired
	CourseRepository courseRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public RegistrationServiceMQ() {
		System.out.println("MQ registration service ");
	}

	// ----- configuration of message queues

	@Autowired
	Queue registrationQueue;


	// ----- end of configuration of message queue

	// receiver of messages from Registration service to add an enrollment to an existing course

	@RabbitListener(queues = "gradebook-queue")
	@Transactional
	public void receive(EnrollmentDTO enrollmentDTO) {

		System.out.println("MQ Received enrollment: " + enrollmentDTO);

		// Check for missing enrollmentDTO parameters.
		if (enrollmentDTO.studentName == null || enrollmentDTO.studentEmail == null) {
			System.out.println("Missing student enrollment parameters.");
			return;
		}

		// Check the course exists in the database.
		Course c = courseRepository.findById(enrollmentDTO.course_id).orElse(null);
		if (c == null) {
			System.out.println("Course id not found: " + enrollmentDTO.course_id);
			return;
		}

		// Create a new Enrollment object using the enrollmentDTO object from the request body
		// and insert it into the database.
		Enrollment enrollment = new Enrollment();
		enrollment.setStudentName(enrollmentDTO.studentName);
		enrollment.setStudentEmail(enrollmentDTO.studentEmail);
		enrollment.setCourse(c);
		enrollmentRepository.save(enrollment);
		System.out.println("MQ Saved enrollment: " + enrollment);
		
	} // end receive

	// sender of messages to Registration Service
	@Override
	public void sendFinalGrades(int course_id, CourseDTOG courseDTO) {

		// Send a RabbitMQ message to the registration service with the final grades in courseDTO.
		System.out.println("MQ Sending final grades: " + course_id + " " + courseDTO);
		rabbitTemplate.convertAndSend(registrationQueue.getName(), courseDTO);
		System.out.println("MQ Grades sent to registration service for course " + course_id + ": " + courseDTO);
		
	} // end sendFinalGrades

} // end RegistrationServiceMQ
