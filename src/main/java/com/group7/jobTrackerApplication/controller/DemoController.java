package com.group7.jobTrackerApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greeting")
public class DemoController {

    @GetMapping
    public String greeting(){
        return "Greetings from Group 7!";
    }
}
