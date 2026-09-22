package eapli.alsafe.remoteaccess.server;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.RemoteAccessDispatcher;
import eapli.alsafe.remoteaccess.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    private static final String LOG_SERVER_HOST = "localhost";
    private static final int LOG_SERVER_PORT = 8888;

    private final Socket clientSocket;
    private final RemoteAccessDispatcher dispatcher;

    private boolean authenticated;
    private boolean gracefulLogout;
    private String authenticatedUsername = "unknown";
    private String authenticatedService = "UNKNOWN";

    public ClientHandler(final Socket socket, final RemoteAccessDispatcher dispatcher) {
        this.clientSocket = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {

            boolean keepRunning = true;

            while (keepRunning) {
                final Packet receivedPacket;
                try {
                    receivedPacket = Utils.receivePacket(in);
                } catch (java.io.IOException e) {
                    logDisconnectIfNeeded();
                    break;
                }

                keepRunning = processPacket(receivedPacket, out);
            }
        } catch (Exception e) {
            System.out.println("[SERVER] Connection closed abruptly or error: " + e.getMessage());
            logDisconnectIfNeeded();
        } finally {
            try {
                clientSocket.close();
                System.out.println("[SERVER] Socket closed safely.");
            } catch (Exception ignored) {
                // socket already closed
            }
        }
    }

    private boolean processPacket(final Packet packet, final OutputStream out) throws Exception {
        if (packet.opCode() == 1) {
            final Packet response = dispatcher.dispatch(packet);
            sendAuthentication(packet, response, out);
            return true;
        } else if (packet.opCode() == 0) {
            gracefulLogout = true;
            if (authenticated) {
                sendUDPDatagram("LOGOUT", authenticatedUsername, authenticatedService);
            }
            final byte[] msg = "[SERVER] Client requested disconnection.".getBytes(StandardCharsets.UTF_8);
            Utils.sendPacket(out, new Packet((byte) 100, msg.length, msg));
            return false;
        }

        final Packet response = dispatcher.dispatch(packet);
        Utils.sendPacket(out, response);
        return true;
    }

    private void sendAuthentication(final Packet request, final Packet response, final OutputStream out) throws Exception {
        final String credentials = new String(request.payload(), StandardCharsets.UTF_8);
        final String[] parts = credentials.trim().split(";", 3);

        final String username = parts.length > 0 ? parts[0] : "unknown";
        final String service = parts.length > 2 ? serviceFromRoles(parts[2]) : "UNKNOWN";
        final boolean loginSuccessful = response.opCode() == 100;

        if (loginSuccessful) {
            authenticated = true;
            authenticatedUsername = username;
            authenticatedService = service;
        }

        sendUDPDatagram(loginSuccessful ? "LOGIN_SUCCESS" : "LOGIN_FAILURE", username, service);
        Utils.sendPacket(out, response);
    }

    private String serviceFromRoles(final String roles) {
        if (roles.contains("AIR_TRANSPORT_COMPANY_COLLABORATOR")) {
            return "US78";
        }
        if (roles.contains("WEATHER_PERSON")) {
            return "US44";
        }
        if (roles.contains("PILOT")) {
            return "US86";
        }
        return "UNKNOWN";
    }

    private void logDisconnectIfNeeded() {
        if (authenticated && !gracefulLogout) {
            sendUDPDatagram("DISCONNECT", authenticatedUsername, authenticatedService);
            gracefulLogout = true;
        }
    }

    private void sendUDPDatagram(final String eventType, final String username, final String service) {
        final String logMessage = String.format(
                "timestamp=%s;event=%s;username=%s;clientIp=%s;clientPort=%d;service=%s",
                LocalDateTime.now(),
                eventType,
                username,
                clientSocket.getInetAddress().getHostAddress(),
                clientSocket.getPort(),
                service
        );
        final byte[] logBytes = logMessage.getBytes(StandardCharsets.UTF_8);

        try (DatagramSocket udpSocket = new DatagramSocket()) {
            final var packet = new DatagramPacket(
                    logBytes,
                    logBytes.length,
                    InetAddress.getByName(LOG_SERVER_HOST),
                    LOG_SERVER_PORT
            );
            udpSocket.send(packet);
            System.out.println("[SERVER -> LOG] " + eventType + " sent via UDP.");
        } catch (Exception e) {
            System.err.println("[SERVER -> LOG] Error sending UDP log: " + e.getMessage());
        }
    }
}
