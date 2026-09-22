package eapli.alsafe.app.remoteaccess;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class LoggingServer {
    private static final int UDP_PORT = 8888;
    private static final int HTTP_PORT = 8880;
    private static final int BUFFER_SIZE = 4096;

    // Thread-safe lists to store the real-time state
    private static final List<LogEntry> eventLogs = new CopyOnWriteArrayList<>();
    private static final Map<String, LogEntry> activeSessions = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("[AUDIT SERVICES] Initializing Logging Services...");

        // 1. Start the HTTP server on a background thread
        startHttpServer(HTTP_PORT);

        // 2. Run the UDP Server on the main thread
        runUdpServer(UDP_PORT);
    }

    private static void runUdpServer(int port) {
        System.out.println("[UDP LOG RECEIVER] Starting on port " + port + "...");
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String logMessage = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                // Parse the received event
                LogEntry entry = LogEntry.parse(logMessage);
                if (entry != null) {
                    // Update lists
                    eventLogs.add(0, entry);
                    if (eventLogs.size() > 100) {
                        eventLogs.remove(eventLogs.size() - 1);
                    }
                    updateActiveSessions(entry);

                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    System.out.printf("[%s] [UDP LOG] [%s] -> %s%n", timestamp, packet.getAddress().getHostAddress(), logMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("[UDP LOG RECEIVER] Critical Error: " + e.getMessage());
        }
    }

    private static void updateActiveSessions(LogEntry entry) {
        String key = entry.username + "@" + entry.clientIp + ":" + entry.clientPort;
        if ("LOGIN_SUCCESS".equals(entry.event)) {
            activeSessions.put(key, entry);
        } else if ("LOGOUT".equals(entry.event) || "DISCONNECT".equals(entry.event)) {
            activeSessions.remove(key);
        }
    }

    private static void startHttpServer(int port) {
        new Thread(() -> {
            System.out.println("[HTTP SERVER] Starting on port " + port + "...");
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handleHttpRequest(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("[HTTP SERVER] Critical Error: " + e.getMessage());
            }
        }).start();
    }

    private static void handleHttpRequest(Socket clientSocket) {
        new Thread(() -> {
            try (InputStream in = clientSocket.getInputStream();
                 OutputStream out = clientSocket.getOutputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

                String line = reader.readLine();
                if (line == null) return;

                String[] requestParts = line.split(" ");
                if (requestParts.length < 2) return;

                String method = requestParts[0];
                String path = requestParts[1];

                // Consume rest of headers
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    // ignore headers
                }

                if ("GET".equalsIgnoreCase(method)) {
                    servePath(path, out);
                } else {
                    send405(out);
                }
            } catch (IOException e) {
                // Ignore client disconnects
            } finally {
                try {
                    clientSocket.close();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    private static void servePath(String path, OutputStream out) throws IOException {
        byte[] body;
        String contentType;
        int status = 200;

        if ("/".equals(path) || "/index.html".equals(path)) {
            body = INDEX_HTML.getBytes(StandardCharsets.UTF_8);
            contentType = "text/html; charset=UTF-8";
        } else if ("/events.html".equals(path)) {
            body = EVENTS_HTML.getBytes(StandardCharsets.UTF_8);
            contentType = "text/html; charset=UTF-8";
        } else if ("/active.html".equals(path)) {
            body = ACTIVE_HTML.getBytes(StandardCharsets.UTF_8);
            contentType = "text/html; charset=UTF-8";
        } else if ("/api/events".equals(path)) {
            body = getEventsJson().getBytes(StandardCharsets.UTF_8);
            contentType = "application/json";
        } else if ("/api/active".equals(path)) {
            body = getActiveJson().getBytes(StandardCharsets.UTF_8);
            contentType = "application/json";
        } else {
            body = "<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8);
            contentType = "text/html; charset=UTF-8";
            status = 404;
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.printf("HTTP/1.1 %d %s\r\n", status, status == 200 ? "OK" : "Not Found");
        writer.printf("Content-Type: %s\r\n", contentType);
        writer.printf("Content-Length: %d\r\n", body.length);
        writer.printf("Connection: close\r\n");
        writer.printf("\r\n");
        writer.flush();

        out.write(body);
        out.flush();
    }

    private static void send405(OutputStream out) throws IOException {
        byte[] body = "<h1>405 Method Not Allowed</h1>".getBytes(StandardCharsets.UTF_8);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        writer.printf("HTTP/1.1 405 Method Not Allowed\r\n");
        writer.printf("Content-Type: text/html; charset=UTF-8\r\n");
        writer.printf("Content-Length: %d\r\n", body.length);
        writer.printf("Connection: close\r\n");
        writer.printf("\r\n");
        writer.flush();
        out.write(body);
        out.flush();
    }

    private static String getEventsJson() {
        List<LogEntry> copy = new ArrayList<>(eventLogs);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < copy.size(); i++) {
            sb.append(copy.get(i).toJson());
            if (i < copy.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String getActiveJson() {
        List<LogEntry> copy = new ArrayList<>(activeSessions.values());
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < copy.size(); i++) {
            sb.append(copy.get(i).toJson());
            if (i < copy.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Static nested class to model a log entry
    static class LogEntry {
        String timestamp;
        String event;
        String username;
        String clientIp;
        int clientPort;
        String service;

        static LogEntry parse(String raw) {
            try {
                LogEntry entry = new LogEntry();
                String[] parts = raw.split(";");
                for (String part : parts) {
                    String[] kv = part.split("=", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String val = kv[1].trim();
                        switch (key) {
                            case "timestamp": entry.timestamp = val; break;
                            case "event": entry.event = val; break;
                            case "username": entry.username = val; break;
                            case "clientIp": entry.clientIp = val; break;
                            case "clientPort": entry.clientPort = Integer.parseInt(val); break;
                            case "service": entry.service = val; break;
                        }
                    }
                }
                return entry;
            } catch (Exception e) {
                System.err.println("[LOG PARSER] Error parsing log message: " + raw + " (" + e.getMessage() + ")");
                return null;
            }
        }

        String toJson() {
            return String.format(
                "{\"timestamp\":\"%s\",\"event\":\"%s\",\"username\":\"%s\",\"clientIp\":\"%s\",\"clientPort\":%d,\"service\":\"%s\"}",
                escapeJson(timestamp), escapeJson(event), escapeJson(username), escapeJson(clientIp), clientPort, escapeJson(service)
            );
        }

        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\"", "\\\"");
        }
    }

    // Embed CSS and HTML pages directly into code for simple, self-contained deployment
    private static final String SHARED_CSS =
        "body {\n" +
        "    font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
        "    background-color: #0f172a;\n" +
        "    color: #f8fafc;\n" +
        "    margin: 0;\n" +
        "    padding: 0;\n" +
        "}\n" +
        ".navbar {\n" +
        "    background: rgba(30, 41, 59, 0.7);\n" +
        "    backdrop-filter: blur(12px);\n" +
        "    border-bottom: 1px solid #334155;\n" +
        "    padding: 16px 24px;\n" +
        "    display: flex;\n" +
        "    justify-content: space-between;\n" +
        "    align-items: center;\n" +
        "    position: sticky;\n" +
        "    top: 0;\n" +
        "    z-index: 100;\n" +
        "}\n" +
        ".navbar-brand {\n" +
        "    font-size: 20px;\n" +
        "    font-weight: 700;\n" +
        "    color: #38bdf8;\n" +
        "    text-decoration: none;\n" +
        "}\n" +
        ".nav-links a {\n" +
        "    color: #94a3b8;\n" +
        "    text-decoration: none;\n" +
        "    margin-left: 20px;\n" +
        "    font-weight: 500;\n" +
        "    transition: color 0.2s;\n" +
        "}\n" +
        ".nav-links a:hover, .nav-links a.active {\n" +
        "    color: #f8fafc;\n" +
        "}\n" +
        ".container {\n" +
        "    max-width: 1200px;\n" +
        "    margin: 40px auto;\n" +
        "    padding: 0 24px;\n" +
        "}\n" +
        ".card {\n" +
        "    background: #1e293b;\n" +
        "    border: 1px solid #334155;\n" +
        "    border-radius: 12px;\n" +
        "    padding: 24px;\n" +
        "    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);\n" +
        "}\n" +
        "h1 {\n" +
        "    font-size: 28px;\n" +
        "    margin-top: 0;\n" +
        "    margin-bottom: 24px;\n" +
        "    color: #f8fafc;\n" +
        "}\n" +
        "table {\n" +
        "    width: 100%;\n" +
        "    border-collapse: collapse;\n" +
        "    margin-top: 16px;\n" +
        "}\n" +
        "th, td {\n" +
        "    padding: 14px 16px;\n" +
        "    text-align: left;\n" +
        "    border-bottom: 1px solid #334155;\n" +
        "}\n" +
        "th {\n" +
        "    color: #94a3b8;\n" +
        "    font-weight: 600;\n" +
        "    text-transform: uppercase;\n" +
        "    font-size: 11px;\n" +
        "    letter-spacing: 0.05em;\n" +
        "}\n" +
        "tr:hover {\n" +
        "    background-color: #1e293b;\n" +
        "}\n" +
        ".badge {\n" +
        "    padding: 4px 10px;\n" +
        "    border-radius: 9999px;\n" +
        "    font-size: 11px;\n" +
        "    font-weight: 600;\n" +
        "    text-transform: uppercase;\n" +
        "    display: inline-block;\n" +
        "}\n" +
        ".badge-success { background-color: #064e3b; color: #34d399; }\n" +
        ".badge-danger { background-color: #7f1d1d; color: #f87171; }\n" +
        ".badge-warning { background-color: #78350f; color: #fbbf24; }\n" +
        ".badge-info { background-color: #1e3a8a; color: #60a5fa; }\n" +
        "@keyframes pulse {\n" +
        "    0%, 100% { opacity: 1; }\n" +
        "    50% { opacity: 0.4; }\n" +
        "}\n";

    private static final String INDEX_HTML =
        "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "    <title>AlSafe Remote Accesses Dashboard</title>\n" +
        "    <style>" + SHARED_CSS + "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <div class=\"navbar\">\n" +
        "        <a href=\"index.html\" class=\"navbar-brand\">AlSafe Audit Control</a>\n" +
        "        <div class=\"nav-links\">\n" +
        "            <a href=\"index.html\" class=\"active\">Home</a>\n" +
        "            <a href=\"events.html\">Events Log</a>\n" +
        "            <a href=\"active.html\">Active Users</a>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "    <div class=\"container\">\n" +
        "        <h1>AlSafe Systems Control Panel</h1>\n" +
        "        <div style=\"display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-top: 32px;\">\n" +
        "            <div class=\"card\" style=\"text-align: center; padding: 40px 24px;\">\n" +
        "                <h2 style=\"color: #38bdf8; font-size: 24px; margin-top:0;\">Remote Access Audit Log</h2>\n" +
        "                <p style=\"color: #94a3b8; margin: 16px 0 32px 0;\">View the complete, real-time chronological log of all login, logout, failed login, and abrupt disconnection events.</p>\n" +
        "                <a href=\"events.html\" style=\"background-color: #0284c7; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; display: inline-block; transition: background 0.2s;\">Go to Audit Logs</a>\n" +
        "            </div>\n" +
        "            <div class=\"card\" style=\"text-align: center; padding: 40px 24px;\">\n" +
        "                <h2 style=\"color: #10b981; font-size: 24px; margin-top:0;\">Active Sessions</h2>\n" +
        "                <p style=\"color: #94a3b8; margin: 16px 0 32px 0;\">Monitor active pilot, weather person, and ATCC sessions currently connected to the remote access system.</p>\n" +
        "                <a href=\"active.html\" style=\"background-color: #059669; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; display: inline-block; transition: background 0.2s;\">Go to Active Sessions</a>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</body>\n" +
        "</html>";

    private static final String EVENTS_HTML =
        "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "    <title>AlSafe Remote Access Events</title>\n" +
        "    <style>" + SHARED_CSS + "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <div class=\"navbar\">\n" +
        "        <a href=\"index.html\" class=\"navbar-brand\">AlSafe Audit Control</a>\n" +
        "        <div class=\"nav-links\">\n" +
        "            <a href=\"index.html\">Home</a>\n" +
        "            <a href=\"events.html\" class=\"active\">Events Log</a>\n" +
        "            <a href=\"active.html\">Active Users</a>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "    <div class=\"container\">\n" +
        "        <div class=\"card\">\n" +
        "            <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;\">\n" +
        "                <h1 style=\"margin: 0;\">Remote Access Events Log</h1>\n" +
        "                <div style=\"display: flex; align-items: center;\">\n" +
        "                    <div style=\"width: 8px; height: 8px; border-radius: 50%; background-color: #10b981; margin-right: 8px; animation: pulse 1.5s infinite;\"></div>\n" +
        "                    <span style=\"color: #94a3b8; font-size: 14px;\">Live Updates (AJAX)</span>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "            <table>\n" +
        "                <thead>\n" +
        "                    <tr>\n" +
        "                        <th>Timestamp</th>\n" +
        "                        <th>Event</th>\n" +
        "                        <th>Username</th>\n" +
        "                        <th>Client Socket</th>\n" +
        "                        <th>Service</th>\n" +
        "                    </tr>\n" +
        "                </thead>\n" +
        "                <tbody id=\"events-table-body\">\n" +
        "                    <tr>\n" +
        "                        <td colspan=\"5\" style=\"text-align: center; color: #94a3b8;\">Waiting for events...</td>\n" +
        "                    </tr>\n" +
        "                </tbody>\n" +
        "            </table>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "\n" +
        "    <script>\n" +
        "        async function fetchEvents() {\n" +
        "            try {\n" +
        "                const response = await fetch('/api/events');\n" +
        "                const data = await response.json();\n" +
        "                const tableBody = document.getElementById('events-table-body');\n" +
        "                if (data.length === 0) {\n" +
        "                    tableBody.innerHTML = '<tr><td colspan=\"5\" style=\"text-align: center; color: #94a3b8;\">No events recorded yet.</td></tr>';\n" +
        "                    return;\n" +
        "                }\n" +
        "                tableBody.innerHTML = '';\n" +
        "                data.forEach(event => {\n" +
        "                    let badgeClass = 'badge-info';\n" +
        "                    if (event.event === 'LOGIN_SUCCESS') badgeClass = 'badge-success';\n" +
        "                    else if (event.event === 'LOGIN_FAILURE') badgeClass = 'badge-danger';\n" +
        "                    else if (event.event === 'DISCONNECT') badgeClass = 'badge-warning';\n" +
        "                    \n" +
        "                    const row = document.createElement('tr');\n" +
        "                    row.innerHTML = `\n" +
        "                        <td>${event.timestamp}</td>\n" +
        "                        <td><span class=\"badge ${badgeClass}\">${event.event}</span></td>\n" +
        "                        <td>${event.username}</td>\n" +
        "                        <td>${event.clientIp}:${event.clientPort}</td>\n" +
        "                        <td>${event.service}</td>\n" +
        "                    `;\n" +
        "                    tableBody.appendChild(row);\n" +
        "                });\n" +
        "            } catch (err) {\n" +
        "                console.error(\"Error fetching events:\", err);\n" +
        "            }\n" +
        "        }\n" +
        "        setInterval(fetchEvents, 2000);\n" +
        "        fetchEvents();\n" +
        "    </script>\n" +
        "</body>\n" +
        "</html>";

    private static final String ACTIVE_HTML =
        "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "    <title>AlSafe Active Remote Users</title>\n" +
        "    <style>" + SHARED_CSS + "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <div class=\"navbar\">\n" +
        "        <a href=\"index.html\" class=\"navbar-brand\">AlSafe Audit Control</a>\n" +
        "        <div class=\"nav-links\">\n" +
        "            <a href=\"index.html\">Home</a>\n" +
        "            <a href=\"events.html\">Events Log</a>\n" +
        "            <a href=\"active.html\" class=\"active\">Active Users</a>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "    <div class=\"container\">\n" +
        "        <div class=\"card\">\n" +
        "            <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;\">\n" +
        "                <h1 style=\"margin: 0;\">Currently Active Sessions</h1>\n" +
        "                <div style=\"display: flex; align-items: center;\">\n" +
        "                    <div style=\"width: 8px; height: 8px; border-radius: 50%; background-color: #10b981; margin-right: 8px; animation: pulse 1.5s infinite;\"></div>\n" +
        "                    <span style=\"color: #94a3b8; font-size: 14px;\">Live Updates (AJAX)</span>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "            <table>\n" +
        "                <thead>\n" +
        "                    <tr>\n" +
        "                        <th>Username</th>\n" +
        "                        <th>Client Socket</th>\n" +
        "                        <th>Service</th>\n" +
        "                        <th>Login Time</th>\n" +
        "                    </tr>\n" +
        "                </thead>\n" +
        "                <tbody id=\"active-table-body\">\n" +
        "                    <tr>\n" +
        "                        <td colspan=\"4\" style=\"text-align: center; color: #94a3b8;\">Checking for active sessions...</td>\n" +
        "                    </tr>\n" +
        "                </tbody>\n" +
        "            </table>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "\n" +
        "    <script>\n" +
        "        async function fetchActive() {\n" +
        "            try {\n" +
        "                const response = await fetch('/api/active');\n" +
        "                const data = await response.json();\n" +
        "                const tableBody = document.getElementById('active-table-body');\n" +
        "                if (data.length === 0) {\n" +
        "                    tableBody.innerHTML = '<tr><td colspan=\"4\" style=\"text-align: center; color: #94a3b8;\">No active sessions currently.</td></tr>';\n" +
        "                    return;\n" +
        "                }\n" +
        "                tableBody.innerHTML = '';\n" +
        "                data.forEach(session => {\n" +
        "                    const row = document.createElement('tr');\n" +
        "                    row.innerHTML = `\n" +
        "                        <td style=\"font-weight: 600; color: #38bdf8;\">${session.username}</td>\n" +
        "                        <td>${session.clientIp}:${session.clientPort}</td>\n" +
        "                        <td><span class=\"badge badge-info\">${session.service}</span></td>\n" +
        "                        <td>${session.timestamp}</td>\n" +
        "                    `;\n" +
        "                    tableBody.appendChild(row);\n" +
        "                });\n" +
        "            } catch (err) {\n" +
        "                console.error(\"Error fetching active sessions:\", err);\n" +
        "            }\n" +
        "        }\n" +
        "        setInterval(fetchActive, 2000);\n" +
        "        fetchActive();\n" +
        "    </script>\n" +
        "</body>\n" +
        "</html>";
}
