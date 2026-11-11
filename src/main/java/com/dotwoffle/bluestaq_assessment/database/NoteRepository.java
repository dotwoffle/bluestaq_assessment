package com.dotwoffle.bluestaq_assessment.database;

import com.dotwoffle.bluestaq_assessment.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {}