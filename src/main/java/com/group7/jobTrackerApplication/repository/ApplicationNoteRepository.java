package com.group7.jobTrackerApplication.repository;

import com.group7.jobTrackerApplication.model.ApplicationNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ApplicationNoteRepository extends JpaRepository<ApplicationNote, Long>{

    Optional<List<ApplicationNote>> findByApplication_ApplicationId(Long applicationId);

    Optional<ApplicationNote> findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(Long notesId, Long applicationId, Long userId);

    List<ApplicationNote> findAllByApplication_User_UserId(Long userId);

    Optional<ApplicationNote> findByApplication_ApplicationIdAndApplication_User_UserId(Long applicationId, Long userId);
}
