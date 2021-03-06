package com.example.mvcstructure.controller;

import com.example.mvcframework.annotaion.HjAutowired;
import com.example.mvcframework.annotaion.HjController;
import com.example.mvcframework.annotaion.HjRequestMapping;
import com.example.mvcframework.annotaion.HjRequestParam;
import com.example.mvcstructure.service.TestService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@HjController
@HjRequestMapping("/test")
public class TestAction {
    @HjAutowired
    private TestService testService;

    @HjRequestMapping("/hello")
    public void hello(@HjRequestParam("name") String name, HttpServletResponse resp) throws IOException {
        testService.hello(name, resp);
    }
}
