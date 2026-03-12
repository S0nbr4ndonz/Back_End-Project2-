package com.group7.jobTrackerApplication.repository;

import com.group7.jobTrackerApplication.model.JobEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobEntryRepository extends JpaRepository<JobEntry, Long> {

    Optional<List<JobEntry>> findByUserId(Long userId);

    Optional<JobEntry> findByJobIdAndUserId(Long jobId, Long userId);
}
