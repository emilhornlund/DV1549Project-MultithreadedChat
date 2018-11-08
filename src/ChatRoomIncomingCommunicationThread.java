import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatRoomIncomingCommunicationThread extends Thread {

    private ChatRoomIncomingCommunicationThreadListener communicationListener;

    private ChatRoomConnectionListener connectionListener;

    private DataInputStream inputStream;

    public ChatRoomIncomingCommunicationThread(Socket socket) throws IOException {
        super();
        System.out.println("ChatRoomIncomingCommunicationThread::init");

        inputStream = new DataInputStream(socket.getInputStream());
    }

    public ChatRoomIncomingCommunicationThreadListener getCommunicationListener() {
        return communicationListener;
    }

    public void setCommunicationListener(ChatRoomIncomingCommunicationThreadListener communicationListener) {
        this.communicationListener = communicationListener;
    }

    public ChatRoomConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public void setConnectionListener(ChatRoomConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    @Override
    public void run() {
        System.out.println("ChatRoomIncomingCommunicationThread::run");

        while (!this.isInterrupted()) {
            try {
                int length = inputStream.readInt();
                if (length != 0 && communicationListener != null) {
                    System.out.println("ChatRoomIncomingCommunicationThread::incomingMessage");
                    byte[] data = new byte[length];
                    inputStream.readFully(data, 0, data.length);
                    ChatPacket packet = ChatPacket.decode(data);
                    communicationListener.receivedPacket(packet);
                }
            } catch (Exception e) {
                connectionListener.socketClosed();
                interrupt();
                break;
            }
        }
    }

    @Override
    public void interrupt() {
        System.out.println("ChatRoomIncomingCommunicationThread::interrupt");

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                System.err.println("Exception");
                e.printStackTrace();
            } finally {
                inputStream = null;
            }
        }
    }

}
