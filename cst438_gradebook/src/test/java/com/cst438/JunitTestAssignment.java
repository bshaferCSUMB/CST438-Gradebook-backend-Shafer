package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;

import com.cst438.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import com.cst438.controllers.AssignmentController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { AssignmentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestAssignment {

  static final String URL = "http://localhost:8080";
  public static final int TEST_COURSE_ID = 40442;
  public static final String TEST_COURSE_TITLE = "Test Course 1";
  public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
  public static final String TEST_STUDENT_NAME = "test";
  public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
  public static final int TEST_YEAR = 2021;
  public static final String TEST_SEMESTER = "Fall";
  public static final int TEST_ASSIGNMENT_ID = 1;
  public static final String TEST_ASSIGNMENT_NAME = "Test Assignment 1";
  public static final String TEST_DUE_DATE = "2023-01-01";

  @MockBean
  AssignmentRepository assignmentRepository;

  @MockBean
  AssignmentGradeRepository assignmentGradeRepository;

  @MockBean
  CourseRepository courseRepository; // must have this to keep Spring test happy

  @MockBean
  RegistrationService registrationService; // must have this to keep Spring test happy

  @Autowired
  private MockMvc mvc;

  @Test
  public void addAssignmentTest() throws Exception {

    MockHttpServletResponse response;

    // Mock database data.
    Course course = new Course();
    course.setCourse_id(TEST_COURSE_ID);
    course.setTitle(TEST_COURSE_TITLE);
    course.setYear(TEST_YEAR);
    course.setInstructor(TEST_INSTRUCTOR_EMAIL);

    Assignment assignment = new Assignment();
    assignment.setCourse(course);
    assignment.setDueDate(java.sql.Date.valueOf(TEST_DUE_DATE));
    assignment.setId(TEST_ASSIGNMENT_ID);
    assignment.setName(TEST_ASSIGNMENT_NAME);
    assignment.setNeedsGrading(1);

    // given -- stubs for database repositories that return test data.
    given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
    given(assignmentRepository.save(assignment)).willReturn(assignment);

    // End of mock data.

    // Create an assignment DTO and set the assignment name and due date.
    AssignmentListDTO.AssignmentDTO testAssignment = new AssignmentListDTO.AssignmentDTO();
    testAssignment.assignmentName = TEST_ASSIGNMENT_NAME;
    testAssignment.dueDate = TEST_DUE_DATE;

    // Do an HTTP Post with the assignment DTO to insert the new assignment.
    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/assignment/" + TEST_COURSE_ID + "/add")
                .content(asJsonString(testAssignment))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();

    // Verify that return status = OK (value 200).
    assertEquals(200, response.getStatus());

    // Verify that returned data has zero for primary key.
    AssignmentListDTO.AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
    assertEquals(0, result.assignmentId);

    // Verify that repository save method was called once.
    verify(assignmentRepository).save(any(Assignment.class));

    // Verify that an invalid due date is caught.
    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/assignment/" + TEST_COURSE_ID + "/add")
                .param("assignmentName", TEST_ASSIGNMENT_NAME)
                .param("dueDate", "invalid date")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
    assertNotEquals(200, response.getStatus());

    // Verify that a missing assignment name is caught.
    response = mvc.perform(
            MockMvcRequestBuilders
                .post("/assignment/" + TEST_COURSE_ID + "/add")
                .param("dueDate", TEST_DUE_DATE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
    assertNotEquals(200, response.getStatus());

    // Verify that repository save method still called only once.
    verify(assignmentRepository, times(1)).save(any(Assignment.class));

  } // End addAssignmentTest

  @Test
  public void changeAssignmentNameTest() throws Exception {

    MockHttpServletResponse response;
    final String NEW_TEST_NAME = "New Assignment Name";

    // Mock database data.
    Course course = new Course();
    course.setCourse_id(TEST_COURSE_ID);
    course.setTitle(TEST_COURSE_TITLE);
    course.setYear(TEST_YEAR);
    course.setInstructor(TEST_INSTRUCTOR_EMAIL);

    Assignment assignment = new Assignment();
    assignment.setId(TEST_ASSIGNMENT_ID);
    assignment.setDueDate(java.sql.Date.valueOf(TEST_DUE_DATE));
    assignment.setName(TEST_ASSIGNMENT_NAME);
    assignment.setNeedsGrading(1);
    assignment.setCourse(course);

    // given -- stubs for database repositories that return test data.
    given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
    given(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).willReturn(Optional.of(assignment));
    given(assignmentRepository.save(assignment)).willReturn(assignment);

    // End of mock data.


    // Create an assignment DTO and set the assignment name to the new name.
    AssignmentListDTO.AssignmentDTO a = new AssignmentListDTO.AssignmentDTO();
    a.assignmentName = NEW_TEST_NAME;

    // Do an HTTP PUT with the path containing the Id of the target assignment
    // and the assignment DTO containing the new name.
    response = mvc.perform(
            MockMvcRequestBuilders
                .put("/assignment/" + TEST_ASSIGNMENT_ID + "/edit")
                .content(asJsonString(a))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();

    // Verify that return status = OK (value 200).
    assertEquals(200, response.getStatus());

    // Verify that returned assignment has the new correct name.
    AssignmentListDTO.AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
    assertEquals(NEW_TEST_NAME, result.assignmentName);

    // Verify that repository save method was called once.
    verify(assignmentRepository, times(1)).save(any(Assignment.class));

    // Verify that an empty name parameter is caught.
    a.assignmentName = null;
    response = mvc.perform(
            MockMvcRequestBuilders
                .put("/assignment/" + TEST_ASSIGNMENT_ID + "/edit")
                .content(asJsonString(a))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
    assertNotEquals(200, response.getStatus());

    // Verify that repository save method still called only once.
    verify(assignmentRepository, times(1)).save(any(Assignment.class));
  } // end changeAssignmentNameTest

  @Test
  public void deleteAssignmentTest() throws Exception {

    MockHttpServletResponse response;

    // Mock database data.
    Course course = new Course();
    course.setCourse_id(TEST_COURSE_ID);
    course.setTitle(TEST_COURSE_TITLE);
    course.setYear(TEST_YEAR);
    course.setInstructor(TEST_INSTRUCTOR_EMAIL);

    Assignment assignment = new Assignment();
    assignment.setId(TEST_ASSIGNMENT_ID);
    assignment.setDueDate(java.sql.Date.valueOf(TEST_DUE_DATE));
    assignment.setName(TEST_ASSIGNMENT_NAME);
    assignment.setNeedsGrading(1);
    assignment.setCourse(course);

    // given -- stubs for database repositories that return test data.
    given(courseRepository.findById(TEST_COURSE_ID)).willReturn(Optional.of(course));
    given(assignmentRepository.findById(TEST_ASSIGNMENT_ID)).willReturn(Optional.of(assignment));
    given(assignmentRepository.save(assignment)).willReturn(assignment);

    // End of mock data.


    // Do an HTTP PUT with the path containing the Id of the target assignment
    // and the assignment DTO containing the new name.
    response = mvc.perform(
            MockMvcRequestBuilders
                .delete("/assignment/" + TEST_ASSIGNMENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();

    // Verify that return status = OK (value 200).
    assertEquals(200, response.getStatus());

    // Verify that repository deleteById method was called once.
    verify(assignmentRepository, times(1)).delete(assignment);

//		 Verify that an invalid assignment_id parameter is caught.
    response = mvc.perform(
            MockMvcRequestBuilders
                .delete("/assignment/" + "invalid" + "/edit")
                .accept(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
    assertNotEquals(200, response.getStatus());

    // Verify that repository deleteById method was called only once.
    verify(assignmentRepository, times(1)).delete(assignment);

  } // end deleteAssignmentTest

  private static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> T fromJsonString(String str, Class<T> valueType) {
    try {
      return new ObjectMapper().readValue(str, valueType);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
