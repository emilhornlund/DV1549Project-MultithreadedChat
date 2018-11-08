import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by emilhornlund on 2017-03-16
 */
public abstract class ChatPacketDispatcherThread extends Thread {
    private LinkedQueue<ChatPacket> packets;

    public ChatPacketDispatcherThread() {
        super();

        System.out.println("ChatPacketDispatcherThread::init");

        this.packets = new LinkedQueue<>();
    }

    public synchronized void sendPacket(ChatPacket packet) {
        System.out.println("ChatPacketDispatcherThread::sendPacket");
        packets.enqueue(packet);
        notify();
    }

    protected synchronized ChatPacket getNextPacket() throws InterruptedException {
        System.out.println("ChatPacketDispatcherThread::getNextPacket");
        while (this.packets.isEmpty()) {
            this.wait();
        }
        ChatPacket packet = this.packets.dequeue();
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        packet.setTime(dateFormat.format(new Date()));
        return packet;
    }
}
