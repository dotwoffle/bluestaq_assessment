package com.dotwoffle.bluestaq_assessment.api;

import com.dotwoffle.bluestaq_assessment.database.NoteRepository;
import com.dotwoffle.bluestaq_assessment.database.TeamRepository;
import com.dotwoffle.bluestaq_assessment.model.Note;
import com.dotwoffle.bluestaq_assessment.model.NoteUpdate;
import com.dotwoffle.bluestaq_assessment.model.Team;
import com.dotwoffle.bluestaq_assessment.model.TeamCreate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class ApiRestController {

    private final NoteRepository noteRepo;
    private final TeamRepository teamRepo;

    public ApiRestController(NoteRepository noteRepo, TeamRepository teamRepo) {
        this.noteRepo = noteRepo;
        this.teamRepo = teamRepo;
    }

    @PostMapping("/create_note")
    public ResponseEntity<Note> createNote(@RequestParam String teamName, @RequestBody Note note) {
        if(!teamRepo.existsById(teamName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Team with name " + teamName + " does not exist");
        }
        if (note.title == null || note.title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (noteRepo.existsById(note.id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A note with that id already exists");
        }

        Team team = teamRepo.getReferenceById(teamName);

        noteRepo.save(new Note(note.id, note.title, note.content));
        team.noteIds.add(note.id);
        teamRepo.save(team);

        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @GetMapping("/get_note")
    public Note getNote(@RequestParam UUID id) {
        return noteRepo.findById(id)
                .map(e -> new Note(e.id, e.title, e.content))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/delete_note")
    public ResponseEntity<Void> deleteNote(@RequestParam UUID id) {
        if (!noteRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No note with that id");
        }
        noteRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update_note")
    public Note updateNote(@RequestParam UUID id, @RequestBody NoteUpdate update) {
        Note note = noteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No note with that id"));

        if (update.title() != null) {
            if (update.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be blank");
            }
            note.title = update.title();
        }
        if (update.content() != null) {
            note.content = update.content();
        }

        return noteRepo.save(note);
    }

    @PostMapping("/create_team")
    public ResponseEntity<Team> createTeam(@RequestBody TeamCreate request) {
        if (request.teamName() == null || request.teamName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "teamName is required");
        }
        if (teamRepo.existsById(request.teamName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "teamName already taken");
        }
        Team saved = teamRepo.save(new Team(request.teamName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}
