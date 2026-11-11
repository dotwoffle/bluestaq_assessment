package com.dotwoffle.bluestaq_assessment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    public UUID id;
    public String title;
    public String content;

    //idk why this is needed yet
    public Note() {
        this.id = UUID.randomUUID();
        this.title = "???";
        this.content = "???";
    }

    public Note(UUID id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}