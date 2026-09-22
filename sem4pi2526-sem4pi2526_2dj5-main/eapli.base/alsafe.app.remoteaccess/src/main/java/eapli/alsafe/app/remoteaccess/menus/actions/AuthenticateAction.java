package eapli.alsafe.app.remoteaccess.menus.actions;

import eapli.alsafe.remoteaccess.client.TCPClient;
import eapli.alsafe.remoteaccess.Packet;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.io.util.Console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AuthenticateAction implements Action {

    private static final int MAX_AUTHENTICATION_ATTEMPTS = 3;

    private final TCPClient client;
    private final Role[] requiredRoles;

    public AuthenticateAction(TCPClient client, Role... requiredRoles) {
        this.client = client;
        this.requiredRoles = requiredRoles;
    }

    @Override
    public boolean execute() {
        for (int attempt = 1; attempt <= MAX_AUTHENTICATION_ATTEMPTS; attempt++) {
            String email = Console.readNonEmptyLine("Email: ", "Please enter an email");
            String password = Console.readLine("Password: ");

            String credentials = email + ";" + password + ";" + Arrays.toString(requiredRoles);
            byte[] payload = credentials.getBytes(StandardCharsets.UTF_8);

            try {
                if (!client.isConnected()) client.connect();

                client.sendPacket(new Packet((byte) 1, payload.length, payload));
                Packet response = client.receivePacket();
                String msg = new String(response.payload(), StandardCharsets.UTF_8);

                System.out.println(msg);

                if (response.opCode() == (byte) 100) return true;

                client.disconnect();
                int remainingAttempts = MAX_AUTHENTICATION_ATTEMPTS - attempt;
                if (remainingAttempts > 0) {
                    System.out.println("You have " + remainingAttempts + " authentication attempt(s) remaining.");
                }
            } catch (IOException e) {
                System.out.println("Error during authentication: " + e.getMessage());
                return false;
            }
        }

        System.out.println("Authentication failed. Maximum number of attempts reached.");
        return false;
    }
}
