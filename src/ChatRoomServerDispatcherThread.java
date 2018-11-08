import java.util.Vector;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatRoomServerDispatcherThread extends ChatPacketDispatcherThread {

    private ChatRoomServer server;

    private Vector<ChatClientConnection> connections;

    private LinkedQueue<ChatPacket> packets;

    public ChatRoomServerDispatcherThread(ChatRoomServer server) {
        super();
        System.out.println("ChatRoomServerDispatcherThread::init");
        this.server = server;
        this.connections = new Vector<>();
    }

    public synchronized void addConnection(ChatClientConnection connection) {
        System.out.println("ChatRoomServerDispatcherThread::addConnection");
        connections.add(connection);
    }

    public synchronized void deleteConnection(ChatClientConnection connection) {
        System.out.println("ChatRoomServerDispatcherThread::deleteConnection");
        int index = connections.indexOf(connection);
        if (index != -1) {
            String nickname = connection.getNickname();

            connection.close();
            connections.removeElementAt(index);

            ChatPacketMessage packetMessage = new ChatPacketMessage("Server", nickname + " left");
            sendPacket(packetMessage);
        }
    }

    public synchronized void processIncomingPacket(ChatPacket packet, ChatClientConnection connection) {
        System.out.println("ChatRoomServerDispatcherThread::processIncomingPacket");
        if (packet instanceof ChatPacketMessage) {
            if (connection.getNickname() == null) {
                ChatPacketMessage packetMessage = new ChatPacketMessage("Server", "Invalid nickname <NICK> nickname");
                connection.sendPacket(packetMessage);
            } else {
                sendPacket(packet);
            }
        } else if (packet instanceof ChatPacketCommand) {
            System.out.println("ChatRoomServerDispatcherThread::processIncomingPacket::ChatPacketCommand");
            ChatPacketCommand packetCommand = (ChatPacketCommand) packet;
            processIncomingPacketCommand(packetCommand, connection);
        } else {
            System.err.println("ChatRoomServerDispatcherThread::processIncomingPacket::unknown");
        }
    }

    private synchronized void processIncomingPacketCommand(ChatPacketCommand packet, ChatClientConnection connection) {
        if (packet.getCommand().equals("NICK") && packet.getNumberOfArguments() == 1) {
            processIncomingPacketNickCommand(packet, connection);
        }
    }

    private synchronized void processIncomingPacketNickCommand(ChatPacketCommand packet, ChatClientConnection connection) {
        String oldNickname = connection.getNickname();
        String newNickname = packet.getArgumentAt(0);
        ChatPacketMessage packetMessage;
        if (!isValidNickname(newNickname)) {
            packetMessage = new ChatPacketMessage("Server", "Invalid nickname <NICK> nickname");
            connection.sendPacket(packetMessage);
        } else {
            connection.setNickname(newNickname);
            connection.sendPacket(packet);

            if (oldNickname == null) {
                packetMessage = new ChatPacketMessage("Server", newNickname + " joined");
            } else {
                packetMessage = new ChatPacketMessage("Server", oldNickname + " changed nickname to " + newNickname);
            }
            sendPacket(packetMessage);
        }
    }

    private boolean isValidNickname(String nickname) {
        boolean valid = true;
        if (nickname == null) {
            valid = false;
        } else if (nickname.toLowerCase().equals("server")) {
            valid = false;
        } else {
            for (ChatClientConnection connection : connections) {
                if (connection.getNickname() != null && connection.getNickname().toLowerCase().equals(nickname.toLowerCase())) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("ChatRoomServerDispatcherThread::run");
        while (!this.isInterrupted()) {
            try {
                ChatPacket packet = getNextPacket();
                server.processIncomingPacket(packet);
                for (ChatClientConnection connection: connections) {
                    connection.sendPacket(packet);
                }
            } catch (Exception e) {
                interrupt();
                break;
            }
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        System.out.println("ChatRoomServerDispatcherThread::interrupt");
        if (!connections.isEmpty()) {
            for (ChatClientConnection connection : connections) {
                connection.close();
            }
        }
        this.connections = new Vector<>();
        this.packets = new LinkedQueue<>();
    }

}
