package eapli.alsafe.usermanagement.application.dto;

import java.time.LocalDate;

/**
 * Read model for listing AlSafe backoffice users (US033).
 */
public record UserDTO(
        String email,
        String name,
        String phoneNumber,
        String role,
        String status,
        LocalDate securityClearanceExpiration,
        LocalDate lastSkillsAssessmentDate) {
}
