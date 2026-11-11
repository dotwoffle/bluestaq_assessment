package com.dotwoffle.bluestaq_assessment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiRestController {

    @GetMapping("/hi")
    private String endpoint() {
        return "hello world";
    }

}
