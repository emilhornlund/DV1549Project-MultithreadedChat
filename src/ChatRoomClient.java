import java.io.IOException;
import java.net.Socket;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatRoomClient extends ChatRoom {

    private class IncomingCommunicationListener implements ChatRoomIncomingCommunicationThreadListener {
        public void receivedPacket(ChatPacket packet) {
            System.out.println("ChatRoomClient::IncomingCommunicationListener::receivedPacket");
            processIncomingPacket(packet);
        }
    }

    private class ConnectionListener implements ChatRoomConnectionListener {
        public void socketClosed() {
            System.out.println("ChatRoomClient::ConnectionListener::socketClosed");
            close();
        }
    }

    private Socket socket;

    private ChatRoomIncomingCommunicationThread incomingCommunicationThread;

    private ChatRoomOutgoingCommunicationThread outgoingCommunicationThread;

    public ChatRoomClient() {
        super();

        System.out.println("Client init");
    }

    public void connect(String hostname, int port) {
        try {
            socket = new Socket(hostname, port);

            this.incomingCommunicationThread = new ChatRoomIncomingCommunicationThread(socket);
            this.incomingCommunicationThread.setCommunicationListener(new IncomingCommunicationListener());
            this.incomingCommunicationThread.setConnectionListener(new ConnectionListener());
            this.incomingCommunicationThread.start();

            this.outgoingCommunicationThread = new ChatRoomOutgoingCommunicationThread(socket);
            this.outgoingCommunicationThread.setConnectionListener(new ConnectionListener());
            this.outgoingCommunicationThread.start();

            notify(ChatRoomEventListener.ChatRoomEvent.CONNECTED);
        } catch (Exception e) {
            String[] args = new String[1];
            args[0] = e.getMessage();
            notify(ChatRoomEventListener.ChatRoomEvent.FAILED_CONNECT, args);
        }
    }

    public boolean isClosed() {
        return !(socket != null && !socket.isClosed() && incomingCommunicationThread != null && !incomingCommunicationThread.isInterrupted() && outgoingCommunicationThread != null && !outgoingCommunicationThread.isInterrupted());
    }

    public void close() {
        if (isClosed())
            return;

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (incomingCommunicationThread != null && !incomingCommunicationThread.isInterrupted()) {
            incomingCommunicationThread.interrupt();
            incomingCommunicationThread = null;
        }

        if (outgoingCommunicationThread != null && !outgoingCommunicationThread.isInterrupted()) {
            outgoingCommunicationThread.interrupt();
            outgoingCommunicationThread = null;
        }

        System.out.println("DISCONNECTED");
        notify(ChatRoomEventListener.ChatRoomEvent.DISCONNECTED);
    }

    public void sendMessage(String message) {
        if (isClosed())
            return;

        String[] parts = message.split(" ");
        if (parts.length == 2 && parts[0].equals("NICK")) {
            ChatPacketCommand packet = new ChatPacketCommand(null, "NICK");
            packet.addArgument(parts[1]);
            this.outgoingCommunicationThread.sendPacket(packet);
        } else {
            ChatPacketMessage packet = new ChatPacketMessage(null, message);
            this.outgoingCommunicationThread.sendPacket(packet);
        }
    }
}
