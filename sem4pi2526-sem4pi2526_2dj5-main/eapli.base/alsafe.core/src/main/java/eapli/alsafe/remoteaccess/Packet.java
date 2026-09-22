package eapli.alsafe.remoteaccess;

import java.util.Arrays;
import java.util.Objects;

public record Packet(byte opCode, int length, byte[] payload) {

    public Packet(byte opCode, int length, byte[] payload) {
        if (length < 0) throw new IllegalArgumentException("Length cannot be negative.");

        if (length == 0) {
            this.payload = new byte[0];
        } else {
            if (payload == null) throw new IllegalArgumentException("Payload cannot be null when the length is a positive value.");

            this.payload = payload.clone();
        }

        this.opCode = opCode;
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet other)) return false;
        return this.opCode == other.opCode && this.length == other.length && Arrays.equals(this.payload, other.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(opCode, length);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
