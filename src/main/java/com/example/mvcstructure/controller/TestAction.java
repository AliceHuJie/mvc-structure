package com.example.mvcstructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestAction {

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "hjdefault") String name) {
        return "hello" + name;
    }
}
