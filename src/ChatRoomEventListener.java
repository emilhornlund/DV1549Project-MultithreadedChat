/**
 * Created by emilhornlund on 2017-03-16
 */
public interface ChatRoomEventListener {
    enum ChatRoomEvent {
        SERVER_STARTED,
        FAILED_START_SERVER,
        SERVER_STOPPED,
        FAILED_STOP_SERVER,
        CONNECTED,
        FAILED_CONNECT,
        DISCONNECTED,
        CHANGE_NICKNAME,
        MESSAGE_RECEIVED,
        ERROR
    };

    void receivedEvent(ChatRoomEvent event, String[] args);
}
