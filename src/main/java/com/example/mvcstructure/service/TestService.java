package com.example.mvcstructure.service;

import com.example.mvcframework.annotaion.HjService;

@HjService
public class TestService {
    public String hello(String name) {
        return "hello" + name;
    }
}
