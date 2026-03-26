package com.group7.jobTrackerApplication.repository;

import com.group7.jobTrackerApplication.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<List<JobApplication>> findAllByUser_UserId(Long userUserId);

    Optional<JobApplication> findByApplicationIdAndUser_UserId(Long applicationId, Long userId );

    long countByUser_UserId(Long userId);
}
