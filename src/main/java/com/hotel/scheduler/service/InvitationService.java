package com.hotel.scheduler.service;

import com.hotel.scheduler.model.Invitation;
import com.hotel.scheduler.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class InvitationService {
    private final InvitationRepository invitationRepository;

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

    @Transactional
    public Invitation createInvitation(Invitation invitation) {
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7)); // default 7 days expiry
        invitation.setUsed(false);
        return invitationRepository.save(invitation);
    }

    @Transactional
    public void markInvitationUsed(String code) {
        invitationRepository.findByCode(code).ifPresent(inv -> {
            inv.setUsed(true);
            invitationRepository.save(inv);
        });
    }

    /**
     * Get all active (not used, not expired) invitations
     */
    public java.util.List<Invitation> getActiveInvitations() {
        LocalDateTime now = LocalDateTime.now();
        return invitationRepository.findAll().stream()
            .filter(inv -> !inv.isUsed() && inv.getExpiresAt().isAfter(now))
            .toList();
    }

    /**
     * Delete invitation by code (if not used/expired)
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
