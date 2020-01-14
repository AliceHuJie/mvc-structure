package com.example.mvcstructure.controller;

import com.example.mvcstructure.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestAction {
    @Autowired
    private TestService testService;

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "陌生人") String name) {
        return testService.hello(name);
    }
}
