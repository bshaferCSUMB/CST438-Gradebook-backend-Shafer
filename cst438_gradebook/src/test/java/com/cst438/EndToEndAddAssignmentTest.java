package com.cst438;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EndToEndAddAssignmentTest {
  public static final String CHROME_DRIVER_FILE_LOCATION
      = "C:/chromedriver_win32/chromedriver.exe";
  public static final String URL = "http://localhost:3000";
  public static final int SLEEP_DURATION = 1000; // 1 second.

  public static final String ASSIGNMENT_NAME = "Test Assignment 1";
  public static final String ASSIGNMENT_DUEDATE = "2023-01-01"; // Date needs to be in the past to show on needs grading list
  public static final String COURSE_ID = "123456";
  public static final String COURSE_NAME = "cst438-software engineering"; // Course name must be correct for the course ID

  public static final String ADD_ASSIGNMENT_BUTTON = "MuiButtonGroup-grouped";
  public static final String ASSIGNMENT_FORM_NAME_ID = "assignmentName";
  public static final String ASSIGNMENT_FORM_COURSEID_ID = "courseId";
  public static final String ASSIGNMENT_FORM_DUEDATE_ID = "input[placeholder='yyyy-mm-dd']";
  public static final String ASSIGNMENT_FORM_ADD_BUTTON_ID = "Add";
  public static final String ASSIGNMENT_FORM_ADD_BUTTON_TEXT = "ADD";
  public static final String ASSIGNMENT_ADD_TOAST_SUCCESS = "Assignment successfully added";
  public static final String ASSIGNMENT_ADD_TOAST_ERROR = "Error when adding";

  @Test
  public void addAssignmentTest() throws Exception {

    // set the driver location and start driver
    //@formatter:off
    //
    // browser property name Java Driver Class
    // ------- ------------------------ ----------------------
    // Edge webdriver.edge.driver EdgeDriver
    // FireFox webdriver.firefox.driver FirefoxDriver
    // IE webdriver.ie.driver InternetExplorerDriver
    // Chrome webdriver.chrome.driver ChromeDriver
    //
    //@formatter:on

    //TODO update the property name for your browser
    System.setProperty("webdriver.chrome.driver",
      CHROME_DRIVER_FILE_LOCATION);

    //TODO update the class ChromeDriver() for your browser
    // For chromedriver 111 need to specify the following options
    ChromeOptions ops = new ChromeOptions();
    ops.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(ops);

    try {
      WebElement we;

      driver.get(URL);
      // must have a short wait to allow time for the page to download
      Thread.sleep(SLEEP_DURATION);

      // Get the "Add Assignment" Button element and verify it is the correct button.
      we = driver.findElement(By.className(ADD_ASSIGNMENT_BUTTON));
      assertEquals("ADD ASSIGNMENT", we.getText());

      // Click the "Add Assignment" Button to open the add assignment dialog.
      we.click();
      Thread.sleep(SLEEP_DURATION);

      // Enter the assignment name and verify.
      we = driver.findElement(By.name(ASSIGNMENT_FORM_NAME_ID));
      we.sendKeys(ASSIGNMENT_NAME);
      assertEquals(ASSIGNMENT_NAME, we.getAttribute("value"));

      // Enter the course ID and verify.
      we = driver.findElement(By.name(ASSIGNMENT_FORM_COURSEID_ID));
      we.sendKeys(COURSE_ID);
      assertEquals(COURSE_ID, we.getAttribute("value"));

      // Enter the due date and verify.
      we = driver.findElement(By.cssSelector(ASSIGNMENT_FORM_DUEDATE_ID));
      we.sendKeys(ASSIGNMENT_DUEDATE);
      assertEquals(ASSIGNMENT_DUEDATE, we.getAttribute("value"));

      // Find and verify the "Add" button then click it to submit the assignment data.
      we = driver.findElement(By.id(ASSIGNMENT_FORM_ADD_BUTTON_ID));
      assertEquals(ASSIGNMENT_FORM_ADD_BUTTON_TEXT, we.getText());
      we.click();
      Thread.sleep(SLEEP_DURATION);

      // Verify the toast success message appears.
      we = driver.findElement(By.className("Toastify__toast-body"));
      assertEquals(ASSIGNMENT_ADD_TOAST_SUCCESS,we.getText());
      Thread.sleep(SLEEP_DURATION);

      // Scroll to the bottom of the assignment list and verify the assignment was added.
      we = driver.findElement(By.cssSelector(".MuiDataGrid-root"));
      int row_count = Integer.parseInt(we.getAttribute("aria-rowcount"));
      for (int x = 0; x < row_count; x++) {
        we = driver.findElement(By.cssSelector("div[data-id='" + x + "']"));
        new Actions(driver).moveToElement(we).perform();
      }
      we = driver.findElement(By.cssSelector(".MuiDataGrid-row.MuiDataGrid-row--lastVisible"));
      assertEquals(ASSIGNMENT_NAME + "\n" + COURSE_NAME + "\n" + ASSIGNMENT_DUEDATE, we.getText());

    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;

    } finally {
      driver.close();
      driver.quit();
    }
  } // end addAssignmentTest

  @Test
  public void invalidAssignmentTest() throws Exception {

    //TODO update the property name for your browser
    System.setProperty("webdriver.chrome.driver",
        CHROME_DRIVER_FILE_LOCATION);

    //TODO update the class ChromeDriver() for your browser
    // For chromedriver 111 need to specify the following options
    ChromeOptions ops = new ChromeOptions();
    ops.addArguments("--remote-allow-origins=*");

    WebDriver driver = new ChromeDriver(ops);

    try {
      WebElement we;

      driver.get(URL);
      // must have a short wait to allow time for the page to download
      Thread.sleep(SLEEP_DURATION);

      // Get the "Add Assignment" Button element and verify it is the correct button.
      we = driver.findElement(By.className(ADD_ASSIGNMENT_BUTTON));
      assertEquals("ADD ASSIGNMENT", we.getText());

      // Click the "Add Assignment" Button to open the add assignment dialog.
      we.click();
      Thread.sleep(SLEEP_DURATION);

      // Find the assignment name input and verify it is empty.
      we = driver.findElement(By.name(ASSIGNMENT_FORM_NAME_ID));
      assertEquals("", we.getAttribute("value"));

      // Find the course ID input and verify it is empty.
      we = driver.findElement(By.name(ASSIGNMENT_FORM_COURSEID_ID));
      assertEquals("", we.getAttribute("value"));

      // Find the due date input and verify it is empty.
      we = driver.findElement(By.cssSelector(ASSIGNMENT_FORM_DUEDATE_ID));
      assertEquals("", we.getAttribute("value"));

      // Find and verify the "Add" button then click it to show user input errors.
      we = driver.findElement(By.id(ASSIGNMENT_FORM_ADD_BUTTON_ID));
      assertEquals(ASSIGNMENT_FORM_ADD_BUTTON_TEXT, we.getText());
      we.click();

      // Verify all missing user input errors are shown.
      List<WebElement> we_list = driver.findElements(By.cssSelector(".MuiFormHelperText-root.Mui-error"));
      assertEquals("Missing assignment name", we_list.get(0).getText());
      assertEquals("Missing course ID", we_list.get(1).getText());
      assertEquals("Missing due date", we_list.get(2).getText());

      // Enter a valid assignment name and verify.
      we = driver.findElement(By.name(ASSIGNMENT_FORM_NAME_ID));
      we.sendKeys(ASSIGNMENT_NAME);
      assertEquals(ASSIGNMENT_NAME, we.getAttribute("value"));

      // Enter a course ID which does not exist in the database.
      we = driver.findElement(By.name(ASSIGNMENT_FORM_COURSEID_ID));
      we.sendKeys("-1");
      assertEquals("-1", we.getAttribute("value"));

      // Enter an invalid due date and verify.
      we = driver.findElement(By.cssSelector(ASSIGNMENT_FORM_DUEDATE_ID));
      we.sendKeys("00");
      assertEquals("00", we.getAttribute("value"));

      // Verify only the invalid due date error is shown.
      we_list = driver.findElements(By.cssSelector(".MuiFormHelperText-root.Mui-error"));
      assertEquals(1, we_list.size());
      assertEquals("Invalid due date", we_list.get(0).getText());

      // Remove the invalid due date and enter a valid one then verify no user input errors are shown.
      String back_space = "\uE003";
      we.sendKeys(back_space + back_space + ASSIGNMENT_DUEDATE);
      assertEquals(ASSIGNMENT_DUEDATE, we.getAttribute("value"));
      we_list = driver.findElements(By.cssSelector(".MuiFormHelperText-root.Mui-error"));
      assertEquals(0, we_list.size());

//      // Find and verify the "Add" button then click it to submit the assignment data with an invalid course ID.
      we = driver.findElement(By.id(ASSIGNMENT_FORM_ADD_BUTTON_ID));
      assertEquals(ASSIGNMENT_FORM_ADD_BUTTON_TEXT, we.getText());
      we.click();
      Thread.sleep(SLEEP_DURATION);

      // Verify the toast error message appears.
      we = driver.findElement(By.className("Toastify__toast-body"));
      assertEquals(ASSIGNMENT_ADD_TOAST_ERROR,we.getText());

    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;

    } finally {
      driver.close();
      driver.quit();
    }
  } // end invalidAssignmentTest
}
