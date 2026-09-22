package eapli.alsafe.remoteaccess.server.requesthandlers;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.usermanagement.domain.Roles;
import eapli.framework.infrastructure.authz.application.AuthenticationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public class AuthenticationRequestHandler implements RemoteAccessRequestHandler {

    private final AuthenticationService authenticationService;

    public AuthenticationRequestHandler() {
        this.authenticationService = AuthzRegistry.authenticationService();
    }

    @Override
    public Packet handle(Packet packet) {
        String credentials = new String(packet.payload(), StandardCharsets.UTF_8);
        String[] parts = credentials.trim().split(";");

        if (parts.length < 3) {
            byte[] errMsg = "Malformed credentials format".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, errMsg.length, errMsg);
        }

        String email = parts[0];
        String password = parts[1];
        String roles = parts[2];

        Optional<UserSession> success = authenticationService.authenticate(email, password, parseRoles(roles));

        if (success.isPresent()) {
            byte[] successMsg = "Login successful!".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 100, successMsg.length, successMsg);
        } else {
            byte[] failMsg = "Invalid credentials".getBytes(StandardCharsets.UTF_8);
            return new Packet((byte) 101, failMsg.length, failMsg);
        }
    }

    private Role[] parseRoles(String roles) {
        String cleaned = roles.replace("[", "").replace("]", "").trim();

        if (cleaned.isEmpty()) return new Role[0];
        return Arrays.stream(cleaned.split(",")).map(String::trim).map(Role::valueOf).toArray(Role[]::new);
    }
}
