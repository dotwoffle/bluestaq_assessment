package com.dotwoffle.bluestaq_assessment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ApiRestController {

    private final NoteRepository repo;

    public ApiRestController(NoteRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/create_note")
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        if (note.title == null || note.title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (repo.existsById(note.title)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A note with that title already exists");
        }
        repo.save(new Note(note.title, note.content));
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @GetMapping("/get_note")
    public Note getNote(@RequestParam String title) {
        return repo.findById(title)
                .map(e -> new Note(e.title, e.content))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
