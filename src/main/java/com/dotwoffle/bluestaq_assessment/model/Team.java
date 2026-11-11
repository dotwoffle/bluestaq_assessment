package com.dotwoffle.bluestaq_assessment.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    public String teamName;

    @ElementCollection
    @CollectionTable(name = "team_notes", joinColumns = @JoinColumn(name = "team_name"))
    @Column(name = "note_id")
    public List<UUID> noteIds = new ArrayList<>();

    protected Team() {}

    public Team(String username) {
        this.teamName = username;
    }
}