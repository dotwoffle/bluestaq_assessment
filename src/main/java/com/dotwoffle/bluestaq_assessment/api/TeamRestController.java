package com.dotwoffle.bluestaq_assessment.api;

import com.dotwoffle.bluestaq_assessment.database.TeamRepository;
import com.dotwoffle.bluestaq_assessment.model.Team;
import com.dotwoffle.bluestaq_assessment.model.TeamCreate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TeamRestController {

    private final TeamRepository teamRepo;

    public TeamRestController(TeamRepository teamRepo) {
        this.teamRepo = teamRepo;
    }

    @PostMapping("/create_team")
    public ResponseEntity<Team> createteam(@RequestBody TeamCreate request) {
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
