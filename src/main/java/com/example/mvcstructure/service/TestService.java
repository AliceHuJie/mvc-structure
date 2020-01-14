package com.example.mvcstructure.service;

import org.springframework.stereotype.Service;

@Service
public class TestService {
    public String hello(String name) {
        return "hello" + name;
    }
}
