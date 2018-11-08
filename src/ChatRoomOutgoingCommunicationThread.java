import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatRoomOutgoingCommunicationThread extends ChatPacketDispatcherThread {

    private DataOutputStream outputStream;

    private ChatRoomConnectionListener connectionListener;

    public ChatRoomOutgoingCommunicationThread(Socket socket) throws IOException {
        super();

        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public ChatRoomConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public void setConnectionListener(ChatRoomConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                ChatPacket packet = getNextPacket();
                byte[] data = packet.getBytes();
                outputStream.writeInt(data.length);
                outputStream.write(data);
            } catch (Exception e) {
                interrupt();
                break;
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();

        if (connectionListener != null) {
            connectionListener.socketClosed();
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                outputStream = null;
            }
        }
    }
}
