package eapli.alsafe.alsafeusermanagement.domain;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.functional.Either;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.strings.StringMixin;
import eapli.framework.validations.Check;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode
public class AlSafeUserEmail implements ValueObject, Comparable<AlSafeUserEmail>, Serializable, StringMixin {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final String email;

    public AlSafeUserEmail(final String address) {
        this.email = address;
    }

    protected AlSafeUserEmail() {
        // for ORM
        email = "";
    }

    /**
     * Factory method.
     *
     * @param address the string of the email address
     *
     * @return the instance of an alsafe email address
     */
    public static AlSafeUserEmail valueOf(final String address) {
        Preconditions.nonEmpty(address, "The email address should neither be null or empty");

        return new AlSafeUserEmail(address);
    }

    /**
     * Factory method.
     *
     * @param address the email address as a string
     *
     * @return either a string of an instance of an alsafe user email
     */
    public static Either<String, AlSafeUserEmail> tryValueOf(final String address) {
        return new Check<String, AlSafeUserEmail>().failIf()
                .isEmpty(address, () -> "The email address should neither be null or empty")
                .elseSucceed(() -> new AlSafeUserEmail(address));
    }

    public static AlSafeUserEmail fromSystemUserEmail(final EmailAddress email) {
        String alSafeUserEmailString = email.toString();
        return new AlSafeUserEmail(alSafeUserEmailString);
    }

    @Override
    public String toString() {
        return email;
    }

    @Override
    public int compareTo(final AlSafeUserEmail o) {
        return email.compareTo(o.email);
    }
}
