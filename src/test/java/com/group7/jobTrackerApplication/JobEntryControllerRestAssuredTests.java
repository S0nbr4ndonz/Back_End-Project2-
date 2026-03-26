package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.GetJobEntryRequest;
import com.group7.jobTrackerApplication.controller.JobEntryController;
import com.group7.jobTrackerApplication.exception.GlobalExceptionHandler;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.JobEntryService;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import({JobEntryController.class, GlobalExceptionHandler.class})
class JobEntryControllerRestAssuredTests extends AbstractRestAssuredIntegrationTest {

    @MockitoBean
    private JobEntryService jobEntryService;

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
    void getAll_returnsJobEntries() {
        when(jobEntryService.getAll(user)).thenReturn(List.of(
                new GetJobEntryRequest("Acme", "Backend Engineer", "$100k", "https://example.com")
        ));

        given()
                .accept(JSON)
        .when()
                .get(url("/job-entries"))
        .then()
                .statusCode(200)
                .body("[0].companyName", equalTo("Acme"))
                .body("[0].jobTitle", equalTo("Backend Engineer"));
    }

    @Test
    void getById_whenMissing_returns404() {
        when(jobEntryService.getById(99L, user)).thenThrow(new ResourceNotFoundException("Job entry not found"));

        given()
                .accept(JSON)
        .when()
                .get(url("/job-entries/99"))
        .then()
                .statusCode(404)
                .body("error", equalTo("Job entry not found"));
    }

    @Test
    void create_returns201() {
        JobEntry created = new JobEntry();
        created.setJobId(77L);
        created.setCompanyName("Acme");
        created.setJobTitle("Backend Engineer");

        when(jobEntryService.create(any(), any(CreateJobEntryRequest.class))).thenReturn(created);

        given()
                .contentType(JSON)
                .body("""
                        {
                          "companyName": "Acme",
                          "salaryText": "$100k",
                          "postingURL": "https://example.com",
                          "jobTitle": "Backend Engineer"
                        }
                        """)
        .when()
                .post(url("/job-entries"))
        .then()
                .statusCode(201)
                .body("jobId", equalTo(77))
                .body("companyName", equalTo("Acme"));
    }
}
