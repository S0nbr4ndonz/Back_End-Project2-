package com.group7.jobTrackerApplication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes simple root endpoints used for smoke testing and security checks.
 */
@RestController
@Tag(name = "Home", description = "Basic endpoints for connectivity and security verification")
public class HomeController {

    /**
     * Returns a simple public health-style message.
     *
     * @return a plain text greeting
     */
    @GetMapping("/")
    @Operation(summary = "Public home endpoint", description = "Returns a simple message that confirms the API is reachable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message returned")
    })
    public String home(){
        return "Hello World";
    }

    /**
     * Returns a simple message from a secured endpoint.
     *
     * @return a plain text secured greeting
     */
    @GetMapping("/secured")
    @Operation(summary = "Secured test endpoint", description = "Returns a simple message that confirms authenticated access works.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public String secured(){
        return "Hello, Secured!";
    }

}
