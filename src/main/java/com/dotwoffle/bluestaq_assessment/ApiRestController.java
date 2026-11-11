package com.dotwoffle.bluestaq_assessment;

import org.springframework.web.bind.annotation.*;

@RestController
public class ApiRestController {

    @PostMapping("/create_note")
    private String endpoint(@RequestBody Note note) {
        System.out.println(note);
        return "ok";
    }

    @GetMapping("/get_note")
    public Note getNote(@RequestParam String title) {
        return new Note(title, "Default content");
    }

}
