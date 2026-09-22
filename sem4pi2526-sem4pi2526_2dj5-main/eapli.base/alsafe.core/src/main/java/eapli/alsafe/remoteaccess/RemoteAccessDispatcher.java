package eapli.alsafe.remoteaccess;

import java.util.Map;

public class RemoteAccessDispatcher {

    private final Map<Byte, RemoteAccessRequestHandler> handlers;

    public RemoteAccessDispatcher(Map<Byte, RemoteAccessRequestHandler> handlers) {
        this.handlers = Map.copyOf(handlers);
    }

    public Packet dispatch(Packet request) {
        RemoteAccessRequestHandler handler = handlers.get(request.opCode());

        if (handler == null) {
            String errMsg = "Unknown operation code: " + request.opCode();
            return new Packet((byte) 99, errMsg.getBytes().length, errMsg.getBytes());
        }

        return handler.handle(request);
    }
}
