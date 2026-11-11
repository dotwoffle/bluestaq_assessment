package com.dotwoffle.bluestaq_assessment.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    public UUID id;
    public String title;
    public String content;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_shared_teams", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "team_name")
    public Set<String> sharedWith = new HashSet<>();

    protected Note() {}

    public Note(UUID id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}