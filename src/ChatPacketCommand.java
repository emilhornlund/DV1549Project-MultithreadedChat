import java.util.ArrayList;

/**
 * Created by emilhornlund on 2017-03-16
 */
public class ChatPacketCommand extends ChatPacket {

    private String command;

    private ArrayList<String> arguments;

    public ChatPacketCommand(String sender, String command) {
        super(sender);

        this.command = command;
        this.arguments = new ArrayList<>();
    }

    public ChatPacketCommand(String sender) {
        this(sender, null);
    }

    public ChatPacketCommand() {
        this(null, null);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    public String getArgumentAt(int index) {
        return arguments.get(index);
    }

    public int getNumberOfArguments() {
        return arguments.size();
    }

}
