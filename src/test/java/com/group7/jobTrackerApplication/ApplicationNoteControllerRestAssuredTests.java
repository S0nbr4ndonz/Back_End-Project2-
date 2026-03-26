package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.controller.ApplicationNoteController;
import com.group7.jobTrackerApplication.exception.GlobalExceptionHandler;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.ApplicationNotesService;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import({ApplicationNoteController.class, GlobalExceptionHandler.class})
class ApplicationNoteControllerRestAssuredTests extends AbstractRestAssuredIntegrationTest {

    @MockitoBean
    private ApplicationNotesService applicationNotesService;

    @MockitoBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUpUser() {
        user = new User();
        user.setUserId(1L);
        when(userService.getOrCreateFromOAuth(any())).thenReturn(user);
    }

    @Test
    void getNoteById_returnsNoteSummary() {
        ApplicationNote note = buildNote();
        note.setNotesId(9L);

        when(applicationNotesService.getNoteById(9L, 4L, user)).thenReturn(note);

        given()
                .accept(JSON)
        .when()
                .get(url("/job-applications/4/note/9"))
        .then()
                .statusCode(200)
                .body("notesId", equalTo(9))
                .body("jobTitle", equalTo("Backend Engineer"))
                .body("company", equalTo("Acme"));
    }

    @Test
    void getNoteById_whenMissing_returns404() {
        when(applicationNotesService.getNoteById(9L, 4L, user))
                .thenThrow(new ResourceNotFoundException("Application Note not found"));

        given()
                .accept(JSON)
        .when()
                .get(url("/job-applications/4/note/9"))
        .then()
                .statusCode(404)
                .body("error", equalTo("Application Note not found"));
    }

    @Test
    void create_returns201() {
        ApplicationNote created = buildNote();
        created.setNotesId(12L);

        when(applicationNotesService.create(any(Long.class), any(CreateApplicationNoteRequest.class), any(User.class))).thenReturn(created);

        given()
                .contentType(JSON)
                .body("""
                        {
                          "content": "Follow up next week",
                          "lastEdited": "2026-03-06T10:00:00",
                          "jobApplication": {
                            "applicationId": 4
                          }
                        }
                        """)
        .when()
                .post(url("/job-applications/4/note"))
        .then()
                .statusCode(201)
                .body("notesId", equalTo(12))
                .body("content", equalTo("Need to follow up"));
    }

    private ApplicationNote buildNote() {
        JobEntry jobEntry = new JobEntry();
        jobEntry.setJobId(7L);
        jobEntry.setJobTitle("Backend Engineer");
        jobEntry.setCompanyName("Acme");

        JobApplication application = new JobApplication();
        application.setApplicationId(4L);
        application.setJobEntry(jobEntry);
        application.setStatus("INTERVIEW");
        application.setDateApplied(LocalDate.of(2026, 3, 1));

        ApplicationNote note = new ApplicationNote();
        note.setApplication(application);
        note.setContent("Need to follow up");
        note.setLastEdited(LocalDateTime.of(2026, 3, 6, 10, 0));
        return note;
    }
}
