package com.example.mvcstructure.controller;

import com.example.mvcframework.annotaion.HjAutowired;
import com.example.mvcframework.annotaion.HjController;
import com.example.mvcframework.annotaion.HjRequestMapping;
import com.example.mvcframework.annotaion.HjRequestParam;
import com.example.mvcstructure.service.TestService;


@HjController
@HjRequestMapping("/test")
public class TestAction {
    @HjAutowired
    private TestService testService;

    @HjRequestMapping("/hello")
    public String hello(@HjRequestParam("name") String name) {
        return testService.hello(name);
    }
}
