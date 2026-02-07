package com.jira.clone.repository;

import com.jira.clone.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByEmailAndAccepted(String email, boolean accepted);
}