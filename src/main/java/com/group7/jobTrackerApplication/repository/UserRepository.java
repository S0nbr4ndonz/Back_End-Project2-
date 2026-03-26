package com.group7.jobTrackerApplication.repository;

import com.group7.jobTrackerApplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for querying and persisting user records.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthProviderAndOauthSubject(String oauthProvider, String oauthSubject);

    Optional<User> findByUsername(String username);
}
