package com.useshiftly.scheduler.service;

import com.useshiftly.scheduler.model.Invitation;
import com.useshiftly.scheduler.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service layer for managing invitation codes for employee registration and onboarding.
 * <p>
 * Handles creation, validation, usage, and deletion of invitation codes.
 * Ensures invitations are not expired or reused, and provides methods for admin management.
 * <p>
 * <b>Usage:</b> Injected into controllers for invitation-related operations.
 */
@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class InvitationService {

    /**
     * Repository for Invitation entity operations.
     */
    private final InvitationRepository invitationRepository;

    /**
     * Validates an invitation by code and token, checking for existence, usage, and expiration.
     *
     * @param code  the invitation code
     * @param token the invitation token
     * @return optional containing the valid invitation, or empty if invalid/expired/used
     */
    public Optional<Invitation> validateInvitation(String code, String token) {
        Optional<Invitation> invitationOpt = invitationRepository.findByCodeAndToken(code, token);
        if (invitationOpt.isEmpty()) {
            log.warn("[DEBUG] No invitation found for code={}, token={}", code, token);
            return Optional.empty();
        }
        Invitation invitation = invitationOpt.get();
        if (invitation.isUsed()) {
            log.warn("[DEBUG] Invitation is already used: code={}, token={}", code, token);
            return Optional.empty();
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("[DEBUG] Invitation is expired: code={}, token={}, expiresAt={}", code, token, invitation.getExpiresAt());
            return Optional.empty();
        }
        log.info("[DEBUG] Invitation is valid: code={}, token={}, email={}, expiresAt={}, used={}",
            code, token, invitation.getEmail(), invitation.getExpiresAt(), invitation.isUsed());
        return Optional.of(invitation);
    }

    /**
     * Creates a new invitation with default expiration and unused status.
     *
     * @param invitation the invitation to create
     * @return the saved Invitation
     */
    @Transactional
    public Invitation createInvitation(Invitation invitation) {
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // default 7 days expiry
        invitation.setUsed(false);
        return invitationRepository.save(invitation);
    }

    /**
     * Marks an invitation as used by its code.
     *
     * @param code the invitation code
     */
    @Transactional
    public void markInvitationUsed(String code) {
        invitationRepository.findByCode(code).ifPresent(inv -> {
            inv.setUsed(true);
            invitationRepository.save(inv);
        });
    }

    /**
     * Retrieves all active (not used, not expired) invitations.
     *
     * @return list of active invitations
     */
    public java.util.List<Invitation> getActiveInvitations() {
        LocalDateTime now = LocalDateTime.now();
        return invitationRepository.findAll().stream()
            .filter(inv -> !inv.isUsed() && inv.getExpiresAt().isAfter(now))
            .toList();
    }

    /**
     * Deletes an invitation by code if it is not used or expired.
     *
     * @param code the invitation code
     * @return true if deleted, false if not found or already used/expired
     */
    @Transactional
    public boolean deleteInvitationByCode(String code) {
        Optional<Invitation> invitationOpt = invitationRepository.findByCode(code);
        if (invitationOpt.isPresent()) {
            Invitation inv = invitationOpt.get();
            if (!inv.isUsed() && inv.getExpiresAt().isAfter(LocalDateTime.now())) {
                invitationRepository.delete(inv);
                return true;
            }
        }
        return false;
    }
}
