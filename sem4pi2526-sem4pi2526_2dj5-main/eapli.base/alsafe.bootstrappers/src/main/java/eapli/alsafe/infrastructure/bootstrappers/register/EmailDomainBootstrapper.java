package eapli.alsafe.infrastructure.bootstrappers.register;

import eapli.alsafe.alsafeusermanagement.repositories.EmailDomainRepository;
import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;

public class EmailDomainBootstrapper implements Action {
    private final EmailDomainRepository repo = PersistenceContext.repositories().domains();

    @Override
    public boolean execute() {
        repo.save("alsafe.com");
        return true;
    }
}
