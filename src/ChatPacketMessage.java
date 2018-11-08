/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatPacketMessage extends ChatPacket {

    String message;

    public ChatPacketMessage(String sender, String message) {
        super(sender);

        this.message = message;
    }

    public ChatPacketMessage(String sender) {
        this(sender, null);
    }

    public ChatPacketMessage() {
        this(null, null);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
