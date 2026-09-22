package eapli.alsafe.remoteaccess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Utils {

    public static void sendPacket(OutputStream out, Packet packet) throws IOException {
        out.write(packet.opCode());

        int length = packet.length();
        byte[] lengthBytes = new byte[4];
        lengthBytes[0] = (byte) ((length >> 24) & 0xFF);
        lengthBytes[1] = (byte) ((length >> 16) & 0xFF);
        lengthBytes[2] = (byte) ((length >> 8) & 0xFF);
        lengthBytes[3] = (byte) (length & 0xFF);

        out.write(lengthBytes);
        if (length > 0) out.write(packet.payload());
        out.flush();
    }

    public static Packet receivePacket(InputStream in) throws IOException {
        int opCodeField = in.read();
        if (opCodeField == -1) throw new IOException("End of stream");
        byte opCode = (byte) opCodeField;

        byte[] lengthBytes = new byte[4];
        int bytesRead = 0;
        while (bytesRead < 4) {
            int res = in.read(lengthBytes, bytesRead, 4 - bytesRead);
            if (res == -1) throw new IOException("Premature End of Stream");
            bytesRead += res;
        }

        int length = ((lengthBytes[0] & 0xFF) << 24) | ((lengthBytes[1] & 0xFF) << 16) | ((lengthBytes[2] & 0xFF) << 8) | (lengthBytes[3] & 0xFF);

        byte[] payload = new byte[length];
        int payloadBytesRead = 0;
        while (payloadBytesRead < length) {
            int res = in.read(payload, payloadBytesRead, length - payloadBytesRead);
            if (res == -1) throw new IOException("Premature End of Stream");
            payloadBytesRead += res;
        }

        return new Packet(opCode, length, payload);
    }

    public static void closeSafely(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
