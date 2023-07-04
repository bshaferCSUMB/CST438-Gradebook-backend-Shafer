package com.cst438.controllers;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

@RestController
public class AssignmentController {

  @Autowired
  AssignmentRepository assignmentRepository;

  @Autowired
  AssignmentGradeRepository assignmentGradeRepository;

  @Autowired
  CourseRepository courseRepository;

  // Insert a new assignment with the assignment name and due date parameters into the database.
  // Returns an assignment DTO of the new assignment when successful.
  @PostMapping("/assignment/{course_id}/add")
  @Transactional
  public AssignmentListDTO.AssignmentDTO addAssignment2(
      @PathVariable int course_id,
      @RequestBody AssignmentListDTO.AssignmentDTO assignment) {

    // Check that this request is from the course instructor.
    String email = "dwisneski@csumb.edu";  // user name (should be instructor's email)
    Course c = courseRepository.findById(course_id).orElse(null);
    if (c == null || !c.getInstructor().equals(email)) {
      throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized. " + course_id);
    }

    // Check for missing assignment name or date parameters.
    if (assignment.assignmentName == null || assignment.dueDate == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing assignment parameters.");
    }

    // Check the due date parameter is a valid date format.
    Date date;
    try {
      date = Date.valueOf(assignment.dueDate);
    } catch (Exception e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date parameter.");
    }

    // Create a new Assignment object using the name and due date parameters from the request body
    // and insert it into the database.
    Assignment a = new Assignment();
    a.setName(assignment.assignmentName);
    a.setDueDate(date);
    a.setCourse(c);
    a.setNeedsGrading(1); // Needs grading defaults to 1.
    assignmentRepository.save(a);
    System.out.println("Added assignment \"" + a.getName() + "\" to course " + a.getCourse().getCourse_id());

    // Return the assignment just created using an assignment DTO.
    return toDTO(a);
  } // End addAssignment

  // Change the name of an assignment.
  @PutMapping("/assignment/{assignment_id}/edit")
  @Transactional
  public AssignmentListDTO.AssignmentDTO changeAssignmentName(@PathVariable int assignment_id, @RequestBody AssignmentListDTO.AssignmentDTO a) {
    // Verify the assignment exists and the user is allowed to change name of it.
    String email = "dwisneski@csumb.edu";  // user name (should be instructor's email)
    Assignment assignment = checkAssignment(assignment_id, email);  // check that user name matches instructor email of the course.

    // Verify the new assignment name is not missing.
    if (a.assignmentName == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing new assignment name parameter.");
    }

    // Change the assignment name and update the database.
    System.out.println("Changing \"" + assignment.getName() + "\" to: " + a.assignmentName);
    assignment.setName(a.assignmentName);
    assignmentRepository.save(assignment);
    return toDTO(assignment); // return the updated assignment
  } // end changeAssignmentName

  // Delete an assignment if there are no grades for the assignment.
  @DeleteMapping("/assignment/{assignment_id}")
  @Transactional
  public void deleteAssignment(@PathVariable int assignment_id) {
    // Verify the assignment exists and the user is allowed to change name of it.
    String email = "dwisneski@csumb.edu";  // user name (should be instructor's email)
    Assignment assignment = checkAssignment(assignment_id, email);  // check that user name matches instructor email of the course.

    // Check no grades exist for the assignment in the assignment_grade repository.
    List<AssignmentGrade> assignments = assignmentGradeRepository.findAllByAssignmentId(assignment_id);
    for (AssignmentGrade grade: assignments) {
      if (grade.getScore().trim().length() > 0) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete assignment. Grades exist for assignment: " + assignment_id);
      }
      else {
        assignmentGradeRepository.delete(grade); // removes any constraints in assignment_grade table
      }
    }
    // Delete the assignment from assignment table.
    assignmentRepository.delete(assignment);
    System.out.println("Deleted assignment: " + assignment_id);
  } // end deleteAssignment

  private Assignment checkAssignment(int assignmentId, String email) {
    // get assignment
    Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
    if (assignment == null) {
      throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Assignment not found. "+assignmentId );
    }
    // check that user is the course instructor
    if (!assignment.getCourse().getInstructor().equals(email)) {
      throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Not Authorized." );
    }
    return assignment;
  } // end checkAssignment

  // Takes an Assignment object and returns an assignment DTO.
  // Used to transfer assignment data to front end.
  private AssignmentListDTO.AssignmentDTO toDTO(Assignment a) {
    return new AssignmentListDTO.AssignmentDTO(
        a.getId(),
        a.getCourse().getCourse_id(),
        a.getName(),
        a.getDueDate().toString(),
        a.getCourse().getTitle());
  } // end toDTO

} // end AssignmentController.java