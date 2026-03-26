package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.UpdateUserRoleRequest;
import com.group7.jobTrackerApplication.controller.UserController;
import com.group7.jobTrackerApplication.exception.GlobalExceptionHandler;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.Role;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Import({UserController.class, GlobalExceptionHandler.class})
class UserControllerRestAssuredTests extends AbstractRestAssuredIntegrationTest {

    @MockitoBean
    private UserService userService;

    @Test
    void getAllUsers_returnsUsers() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("octocat");
        user.setRole(Role.USER);

        when(userService.getAllUsers()).thenReturn(List.of(user));

        given()
                .accept(JSON)
        .when()
                .get(url("/admin"))
        .then()
                .statusCode(200)
                .body("[0].username", equalTo("octocat"))
                .body("[0].role", equalTo("USER"));
    }

    @Test
    void getUserById_whenMissing_returns404() {
        when(userService.getUserById(77L)).thenThrow(new ResourceNotFoundException("User not found with id: 77"));

        given()
                .accept(JSON)
        .when()
                .get(url("/admin/77"))
        .then()
                .statusCode(404)
                .body("error", equalTo("User not found with id: 77"));
    }

    @Test
    void patch_returnsUpdatedUser() {
        User updated = new User();
        updated.setUserId(2L);
        updated.setUsername("admin-user");
        updated.setRole(Role.ADMIN);

        when(userService.update(any(Long.class), any(UpdateUserRoleRequest.class))).thenReturn(updated);

        given()
                .contentType(JSON)
                .body("""
                        {
                          "role": "ADMIN"
                        }
                        """)
        .when()
                .patch(url("/admin/2"))
        .then()
                .statusCode(200)
                .body("userId", equalTo(2))
                .body("role", equalTo("ADMIN"));
    }
}
