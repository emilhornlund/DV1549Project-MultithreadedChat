import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatRoomServerThread extends Thread {

    private ChatRoomServer server;

    private ServerSocket socket;

    public ChatRoomServerThread(ChatRoomServer server) {
        super();
        System.out.println("ChatRoomServerThread::init");

        this.server = server;
    }

    public void start(int port) {
        System.out.println("ChatRoomServerThread::start");

        if (!isClosed())
            close();

        try {
            this.socket = new ServerSocket(port);
            server.notify(ChatRoomEventListener.ChatRoomEvent.SERVER_STARTED);
        } catch (Exception e) {
            String[] args = new String[1];
            args[0] = e.getMessage();
            server.notify(ChatRoomEventListener.ChatRoomEvent.FAILED_START_SERVER, args);
        }

        this.start();
    }

    @Override
    public void run() {
        super.run();
        System.out.println("ChatRoomServerThread::run");

        if (isClosed()) {
            System.out.println("ChatRoomServerThread::run::isClosed");
            server.notify(ChatRoomEventListener.ChatRoomEvent.SERVER_STOPPED);
            return;
        }

        ChatPacketMessage packetMessage = new ChatPacketMessage("Server", "Server started");
        server.getDispatcherThread().sendPacket(packetMessage);

        while (!isInterrupted()) {
            try {
                Socket socket = this.socket.accept();
                System.out.println("ChatRoomServerThread::run::accept");

                ChatClientConnection connection = new ChatClientConnection(socket, server.getDispatcherThread());
                server.getDispatcherThread().addConnection(connection);
            } catch (Exception e) {
                interrupt();
                break;
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();

        System.out.println("ChatRoomServerThread::interrupt");

        if (!isClosed())
            close();
    }

    public boolean isClosed() {
        return (socket == null || socket.isClosed());
    }

    public void close() {
        System.out.println("ChatRoomServerThread::close");

        if (isClosed()) {
            return;
        }

        try {
            socket.close();
            server.notify(ChatRoomEventListener.ChatRoomEvent.SERVER_STOPPED);
        } catch (IOException ioe) {
            String[] args = new String[1];
            args[0] = ioe.getMessage();
            server.notify(ChatRoomEventListener.ChatRoomEvent.FAILED_STOP_SERVER, args);
        }
    }

}
