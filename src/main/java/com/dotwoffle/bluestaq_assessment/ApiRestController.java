package com.dotwoffle.bluestaq_assessment;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiRestController {

    @PostMapping("/create_note")
    private String endpoint(@RequestBody String note) {
        System.out.println(note);
        return note;
    }

}
