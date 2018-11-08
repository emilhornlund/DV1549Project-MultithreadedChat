import java.io.*;

/**
 * Created by emilhornlund on 2017-03-16
 */
public abstract class ChatPacket implements Serializable {

    private String time;

    private String sender;

    public ChatPacket(String sender) {
        this.time = "00:00:00";
        this.sender = sender;
    }

    public ChatPacket() {
        this(null);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        }
    }

    public static ChatPacket decode(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (ChatPacket) in.readObject();
        }
    }

}
