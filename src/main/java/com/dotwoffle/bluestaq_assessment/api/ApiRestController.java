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

    private Team requireTeamExists(String teamName) {
        return teamRepo.findById(teamName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Team with name " + teamName + " does not exist"));
    }

    private void requireTeamHasNote(Team team, UUID id) {
        if (!team.noteIds.contains(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No note with that id in team " + team.teamName);
        }
    }

    @PostMapping("/notes/create")
    public ResponseEntity<Note> createNote(@RequestParam String teamName, @RequestBody Note note) {
        Team team = teamRepo.findById(teamName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Team with name " + teamName + " does not exist"));

        if (note.title == null || note.title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (noteRepo.existsById(note.id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A note with that id already exists");
        }

        noteRepo.save(new Note(note.id, note.title, note.content));
        team.noteIds.add(note.id);
        teamRepo.save(team);

        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @GetMapping("/notes/get")
    public Note getNote(@RequestParam String teamName, @RequestParam UUID id) {
        System.out.println(teamName);
        System.out.println(id);
        Team team = requireTeamExists(teamName);
        requireTeamHasNote(team, id);

        return noteRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/notes/delete")
    public ResponseEntity<Void> deleteNote(@RequestParam String teamName, @RequestParam UUID id) {
        Team team = requireTeamExists(teamName);
        requireTeamHasNote(team, id);

        noteRepo.deleteById(id);
        team.noteIds.remove(id);
        teamRepo.save(team);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/notes/update")
    public Note updateNote(@RequestParam String teamName, @RequestParam UUID id,
            @RequestBody NoteUpdate update) {
        Team team = requireTeamExists(teamName);
        requireTeamHasNote(team, id);

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

    @PostMapping("/teams/create")
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
