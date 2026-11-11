package com.dotwoffle.bluestaq_assessment.database;

import com.dotwoffle.bluestaq_assessment.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, String> {}