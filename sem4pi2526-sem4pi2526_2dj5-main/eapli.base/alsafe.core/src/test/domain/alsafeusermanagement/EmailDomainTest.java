package domain.alsafeusermanagement;

import eapli.alsafe.alsafeusermanagement.domain.EmailDomain;
import org.junit.Test;

import static org.junit.Assert.*;

public class EmailDomainTest {

    @Test
    public void ensureEmailDomainCanBeCreated() {
        String domain = "@airport.com";
        EmailDomain emailDomain = new EmailDomain(domain);
        assertEquals(domain, emailDomain.getDomain());
    }

    @Test
    public void ensureEmailDomainEqualsWithSameDomain() {
        String domain1 = "@airport.com";
        String domain2 = "@airport.com";

        EmailDomain emailDomain1 = new EmailDomain(domain1);
        EmailDomain emailDomain2 = new EmailDomain(domain2);

        assertEquals(emailDomain1, emailDomain2);
    }

    @Test
    public void ensureEmailDomainSameAsWorks() {
        String domain = "@airport.com";

        EmailDomain emailDomain1 = new EmailDomain(domain);
        EmailDomain emailDomain2 = new EmailDomain(domain);

        assertEquals(emailDomain1.sameAs(emailDomain2), emailDomain2.sameAs(emailDomain1));
    }

    @Test
    public void ensureEmailDomainToStringWorks() {
        String domain = "@airport.com";
        String toString = "EmailDomain : " + domain;

        EmailDomain emailDomain = new EmailDomain(domain);

        assertEquals(emailDomain.toString(), toString);
    }

    @Test
    public void ensureEmailDomainIdentityWorks() {
        String domain = "@airport.com";

        EmailDomain emailDomain = new EmailDomain(domain);

        assertEquals(domain, emailDomain.identity());
    }
}
