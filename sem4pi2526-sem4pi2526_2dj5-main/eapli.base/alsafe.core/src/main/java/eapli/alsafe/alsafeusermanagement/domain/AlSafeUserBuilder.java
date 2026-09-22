package eapli.alsafe.alsafeusermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eapli.framework.domain.model.DomainFactory;

import java.util.Calendar;

public class AlSafeUserBuilder implements DomainFactory<AlSafeUser> {

    private static final Logger LOGGER = LogManager.getLogger(AlSafeUserBuilder.class);

    private SystemUser systemUser;
    private AlSafeUserEmail email;
    private String phoneNumber;
    private SecurityClearance securityClearance;
    private SkillsAssessment skillsAssessment;

    public AlSafeUserBuilder() {}

    /**
     * Helper for the most common and mandatory properties of an AlSafeUser
     *
     * @param systemUser the system user associated
     * @param phoneNumber the phone number of the user
     * @param createdOn the date of creation of the user
     * @return this builder
     */
    public AlSafeUserBuilder with(final SystemUser systemUser, final String phoneNumber,
                                  final Calendar createdOn) {
        withSystemUser(systemUser);
        withEmail(AlSafeUserEmail.fromSystemUserEmail(systemUser.email()));
        withPhoneNumber(phoneNumber);
        withSecurityClearance(createdOn);
        withSkillsAssessment(createdOn);
        return this;
    }

    /**
     * Sets the system user attributed to the AlSafeUser.
     *
     * @param systemUser the system user associated
     * @return this builder
     */
    public AlSafeUserBuilder withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    /**
     * Sets the email to the AlSafeUser.
     *
     * @param email the system user associated's email
     * @return this builder
     */
    public AlSafeUserBuilder withEmail(final AlSafeUserEmail email) {
        this.email = email;
        return this;
    }

    public AlSafeUserBuilder withEmail(final String email) {
        this.email = new AlSafeUserEmail(email);
        return this;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phoneNumber the phone number of the user
     * @return this builder
     */
    public AlSafeUserBuilder withPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Sets the security clearance of the user.
     *
     * @param createdOn the date of creation of the user
     * @return this builder
     */
    public AlSafeUserBuilder withSecurityClearance(final Calendar createdOn) {
        Calendar clearanceDate = (Calendar) createdOn.clone();
        clearanceDate.add(Calendar.YEAR, 2);

        this.securityClearance = new SecurityClearance(clearanceDate);
        return this;
    }

    /**
     * Sets the skills assessment of the user.
     *
     * @param createdOn the date of creation of the user
     * @return this builder
     */
    public AlSafeUserBuilder withSkillsAssessment(final Calendar createdOn) {
        Calendar assessmentDate = (Calendar) createdOn.clone();
        assessmentDate.add(Calendar.YEAR, 5);

        this.skillsAssessment = new SkillsAssessment(assessmentDate);
        return this;
    }

    @Override
    public AlSafeUser build() {
        return new AlSafeUser(systemUser, phoneNumber, securityClearance, skillsAssessment);
    }
}
