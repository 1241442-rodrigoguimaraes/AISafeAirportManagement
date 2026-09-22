package eapli.alsafe.remoteaccess.client;

import eapli.alsafe.remoteaccess.Packet;
import eapli.alsafe.remoteaccess.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TCPClient {
    private final String SERVER_IP;
    private final int SERVER_PORT;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public TCPClient(String serverIP, int serverPort) {
        this.SERVER_IP = serverIP;
        this.SERVER_PORT = serverPort;
    }

    public void connect() throws IOException {
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
    }

    public void sendPacket(Packet packet) throws IOException {
        Utils.sendPacket(out, packet);
    }

    public Packet receivePacket() throws IOException {
        return Utils.receivePacket(in);
    }

    public void disconnect() {
        Utils.closeSafely(in, out, socket);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
