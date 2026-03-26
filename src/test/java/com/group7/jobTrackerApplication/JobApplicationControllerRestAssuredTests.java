package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.GetJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.controller.JobApplicationController;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import com.group7.jobTrackerApplication.exception.GlobalExceptionHandler;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.JobApplicationService;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import({JobApplicationController.class, GlobalExceptionHandler.class})
class JobApplicationControllerRestAssuredTests extends AbstractRestAssuredIntegrationTest {

    @MockitoBean
    private JobApplicationService jobApplicationService;

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
    void getAll_returnsApplications() {
        when(jobApplicationService.getAll(user)).thenReturn(List.of(
                new GetJobApplicationRequest(1L, 10L, "SWE Intern", "APPLIED", LocalDate.of(2026, 3, 1), 5L)
        ));

        given()
                .accept(JSON)
        .when()
                .get(url("/job-applications"))
        .then()
                .statusCode(200)
                .body("[0].jobTitle", equalTo("SWE Intern"))
                .body("[0].status", equalTo("APPLIED"));
    }

    @Test
    void patch_whenForbidden_returns403() {
        when(jobApplicationService.patch(any(Long.class), any(UpdateJobApplicationRequest.class), any(User.class)))
                .thenThrow(new ForbiddenException("Not authorized to update requested job application."));

        given()
                .contentType(JSON)
                .body("""
                        {
                          "jobId": 10,
                          "status": "OFFER",
                          "dateApplied": "2026-03-01"
                        }
                        """)
        .when()
                .patch(url("/job-applications/44"))
        .then()
                .statusCode(403)
                .body("error", equalTo("Not authorized to update requested job application."));
    }

    @Test
    void create_returns201() {
        JobApplication created = new JobApplication();
        created.setApplicationId(44L);
        created.setStatus("APPLIED");
        created.setDateApplied(LocalDate.of(2026, 3, 1));

        when(jobApplicationService.create(any(CreateJobApplicationRequest.class), any(User.class))).thenReturn(created);

        given()
                .contentType(JSON)
                .body("""
                        {
                          "jobId": 10,
                          "status": "APPLIED",
                          "dateApplied": "2026-03-01"
                        }
                        """)
        .when()
                .post(url("/job-applications"))
        .then()
                .statusCode(201)
                .body("applicationId", equalTo(44))
                .body("status", equalTo("APPLIED"));
    }
}
