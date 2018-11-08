/**
 * Created by emilhornlund on 2017-03-16
 */
public abstract class ChatRoom {

    private ChatRoomEventListener listener;

    public ChatRoom() {

    }

    public ChatRoomEventListener getEventListener() {
        return listener;
    }

    public void setEventListener(ChatRoomEventListener listener) {
        this.listener = listener;
    }

    public abstract boolean isClosed();

    public abstract void close();

    protected void processIncomingPacket(ChatPacket packet) {
        if (packet instanceof ChatPacketMessage) {
            ChatPacketMessage packetMessage = (ChatPacketMessage) packet;
            notify(ChatRoomEventListener.ChatRoomEvent.MESSAGE_RECEIVED, new String[] {packetMessage.getTime(), packetMessage.getSender(), packetMessage.getMessage()});
        } else if (packet instanceof ChatPacketCommand) {
            ChatPacketCommand packetCommand = (ChatPacketCommand) packet;
            if (packetCommand.getCommand().equals("NICK") && packetCommand.getNumberOfArguments() == 1) {
                String newNickname = packetCommand.getArgumentAt(0);
                notify(ChatRoomEventListener.ChatRoomEvent.CHANGE_NICKNAME, new String[] {newNickname});
            }
        }
    }

    public abstract void sendMessage(String message);

    public void notify(ChatRoomEventListener.ChatRoomEvent event, String[] args) {
        if (listener != null) {
            listener.receivedEvent(event, args);
        }
    }

    public void notify(ChatRoomEventListener.ChatRoomEvent event) {
        notify(event, null);
    }

}
