package com.dotwoffle.bluestaq_assessment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    public String title;
    public String content;

    //idk why this is needed yet
    public Note() {
        this.title = "???";
        this.content = "???";
    }

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }
}