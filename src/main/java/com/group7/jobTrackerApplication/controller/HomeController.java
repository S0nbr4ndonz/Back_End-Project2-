package com.group7.jobTrackerApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [Brief one-sentence description of what this class does.
 *
 * @author Drew "Dr.C" Clinkenbeard
 * @oversion 0.1.0
 * @since 2026-03-01
 */

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "Hello World";
    }

    @GetMapping("/secured")
    public String secured(){
        return "Hello, Secured!";
    }

}
