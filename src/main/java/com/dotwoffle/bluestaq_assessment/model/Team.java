package com.dotwoffle.bluestaq_assessment.model;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    public String teamName;

    @ElementCollection
    @CollectionTable(name = "team_notes", joinColumns = @JoinColumn(name = "team_name"))
    @Column(name = "note_id")
    public Set<UUID> noteIds = new HashSet<>();

    protected Team() {}

    public Team(String username) {
        this.teamName = username;
    }
}