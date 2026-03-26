package com.group7.jobTrackerApplication;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        classes = RestAssuredTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
                "spring.sql.init.mode=never",
                "spring.jpa.defer-datasource-initialization=false",
                "spring.datasource.url=",
                "spring.datasource.username=",
                "spring.datasource.password="
        }
)
abstract class AbstractRestAssuredIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.reset();
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    protected String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }
}
