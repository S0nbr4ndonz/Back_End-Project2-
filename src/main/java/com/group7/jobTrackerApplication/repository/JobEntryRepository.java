package com.group7.jobTrackerApplication.repository;

import com.group7.jobTrackerApplication.model.JobEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for querying and persisting job entry records.
 */
public interface JobEntryRepository extends JpaRepository<JobEntry, Long> {

    Optional<List<JobEntry>> findByUser_UserId(Long userId);

    Optional<JobEntry> findByJobIdAndUser_UserId(Long jobId, Long userId);
}
