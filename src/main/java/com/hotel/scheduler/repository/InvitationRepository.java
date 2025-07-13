package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByCodeAndToken(String code, String token);
    Optional<Invitation> findByCode(String code);
}
