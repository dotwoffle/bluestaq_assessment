package com.dotwoffle.bluestaq_assessment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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
        if (repo.existsById(note.id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A note with that id already exists");
        }
        repo.save(new Note(note.id, note.title, note.content));
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @GetMapping("/get_note")
    public Note getNote(@RequestParam UUID id) {
        return repo.findById(id)
                .map(e -> new Note(e.id, e.title, e.content))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/delete_note")
    public ResponseEntity<Void> deleteNote(@RequestParam UUID id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No note with that id");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
