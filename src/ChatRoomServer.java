/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatRoomServer extends ChatRoom {

    private ChatRoomServerThread serverThread;

    private ChatRoomServerDispatcherThread dispatcherThread;

    public ChatRoomServer() {
        super();

        System.out.println("Server init");
    }

    public ChatRoomServerThread getServerThread() {
        return serverThread;
    }

    public ChatRoomServerDispatcherThread getDispatcherThread() {
        return dispatcherThread;
    }

    public void open(int port) {
        dispatcherThread = new ChatRoomServerDispatcherThread(this);
        dispatcherThread.start();

        serverThread = new ChatRoomServerThread(this);
        serverThread.start(port);
    }

    public boolean isClosed() {
        return !(serverThread != null && !serverThread.isInterrupted() && dispatcherThread != null && !dispatcherThread.isInterrupted());
    }

    public void close() {
        if (serverThread != null) {
            serverThread.interrupt();
        }

        if (dispatcherThread != null) {
            dispatcherThread.interrupt();
        }
    }

    public void sendMessage(String message) {
        if (isClosed())
            return;
        ChatPacketMessage packet = new ChatPacketMessage("Server", message);
        dispatcherThread.sendPacket(packet);
    }

}
