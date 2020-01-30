package com.example.mvcstructure.service;

import com.example.mvcframework.annotaion.HjService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@HjService
public class TestService {
    public void hello(String name, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("hello " + name);
    }
}
