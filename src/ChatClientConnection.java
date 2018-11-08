import java.io.IOException;
import java.net.Socket;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatClientConnection {

    private class IncomingCommunicationListener implements ChatRoomIncomingCommunicationThreadListener {
        public void receivedPacket(ChatPacket packet) {
            packet.setSender(getNickname());
            dispatcherThread.processIncomingPacket(packet, ChatClientConnection.this);
        }
    }

    private class ConnectionListener implements ChatRoomConnectionListener {
        public void socketClosed() {
            dispatcherThread.deleteConnection(ChatClientConnection.this);
        }
    }

    private Socket socket;

    private ChatRoomServerDispatcherThread dispatcherThread;

    private ChatRoomIncomingCommunicationThread incomingCommunicationThread;

    private ChatRoomOutgoingCommunicationThread outgoingCommunicationThread;

    private String nickname;

    public ChatClientConnection(Socket socket, ChatRoomServerDispatcherThread dispatcherThread) throws IOException {
        super();

        ConnectionListener connectionListener = new ConnectionListener();

        this.socket = socket;
        this.dispatcherThread = dispatcherThread;

        this.incomingCommunicationThread = new ChatRoomIncomingCommunicationThread(socket);
        this.incomingCommunicationThread.setCommunicationListener(new IncomingCommunicationListener());
        this.incomingCommunicationThread.setConnectionListener(connectionListener);
        this.incomingCommunicationThread.start();

        this.outgoingCommunicationThread = new ChatRoomOutgoingCommunicationThread(socket);
        this.outgoingCommunicationThread.setConnectionListener(connectionListener);
        this.outgoingCommunicationThread.start();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                this.socket.close();
            } catch (Exception e) {
                System.err.println("Exception");
                e.printStackTrace();
            } finally {
                this.socket = null;
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
    }

    public void sendPacket(ChatPacket packet) {
        if (!this.outgoingCommunicationThread.isInterrupted()) {
            this.outgoingCommunicationThread.sendPacket(packet);
        }
    }

}
