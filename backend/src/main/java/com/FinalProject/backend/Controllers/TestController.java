package com.FinalProject.backend.Controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @PostMapping("/hello")
    public String hello(

    ) {
        return "Hello, World!";
    }
}
