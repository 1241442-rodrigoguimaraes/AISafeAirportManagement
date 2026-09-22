package eapli.alsafe.app.remoteaccess;

import eapli.alsafe.infrastructure.persistence.PersistenceContext;
import eapli.alsafe.remoteaccess.RemoteAccessDispatcher;
import eapli.alsafe.remoteaccess.RemoteAccessHandlerRegistry;
import eapli.alsafe.remoteaccess.RemoteAccessRequestHandler;
import eapli.alsafe.remoteaccess.server.ClientHandler;
import eapli.alsafe.usermanagement.domain.AlSafePasswordPolicy;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class RemoteServer {

    private static final int SERVER_PORT = 2223;

    public static void main(String[] args) {
        try {
            AuthzRegistry.configure(PersistenceContext.repositories().users(), new AlSafePasswordPolicy(),
                    new PlainTextEncoder());

            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                System.out.println("[SERVER] TCP Remote Access Server started on port " + SERVER_PORT);

                Map<Byte, RemoteAccessRequestHandler> handlers = RemoteAccessHandlerRegistry.handlers();
                RemoteAccessDispatcher dispatcher = new RemoteAccessDispatcher(handlers);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(clientSocket, dispatcher);
                    new Thread(handler).start();
                }
            }
        } catch (Exception e) {
            System.err.println("Error with the Server: " + e.getClass().getName());
            e.printStackTrace();
        }
    }
}
