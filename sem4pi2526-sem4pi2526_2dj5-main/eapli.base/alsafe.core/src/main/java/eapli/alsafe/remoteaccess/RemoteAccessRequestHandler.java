package eapli.alsafe.remoteaccess;

public interface RemoteAccessRequestHandler {
    Packet handle(Packet packet);
}
