import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Created by emilhornlund on 2017-03-17
 */
public class MainGUI extends JFrame {

    private enum Status {DISCONNECTED, RUNNING, CONNECTED};

    //region Listeners

    public class GUIWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            close();
            System.exit(0);
        }
    }

    public class GUIActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case HostButtonActionCommand:
                    if (room != null && !room.isClosed()) {
                        close();
                    } else if (validateHostnameMissingInput()) {
                        showWarningDialog("Warning", "Missing hostname");
                    } else if (validatePortMissingInput()) {
                        showWarningDialog("Warning", "Missing port");
                    } else if (validatePortNumberInput()) {
                        showWarningDialog("Warning", "Invalid port number");
                    } else if (validateNicknameMissingInput()) {
                        showWarningDialog("Warning", "Missing nickname");
                    } else {
                        int port = Integer.parseInt(portTextField.getText());
                        room = host(port);
                    }
                    break;
                case JoinButtonActionCommand:
                    if (room != null && !room.isClosed()) {
                        close();
                    } else if (validateHostnameMissingInput()) {
                        showWarningDialog("Warning", "Missing hostname");
                    } else if (validatePortMissingInput()) {
                        showWarningDialog("Warning", "Missing port");
                    } else if (validatePortNumberInput()) {
                        showWarningDialog("Warning", "Invalid port number");
                    } else if (validateNicknameMissingInput()) {
                        showWarningDialog("Warning", "Missing nickname");
                    } else {
                        int port = Integer.parseInt(portTextField.getText());
                        room = join(hostTextField.getText(), port, nickTextField.getText());
                    }
                    break;
                case MessageTextFieldActionCommand:
                    if (room != null && !room.isClosed()) {
                        room.sendMessage(messageTextField.getText());
                        messageTextField.setText("");
                    } else {
                        showWarningDialog("Warning", "First open a new room or connect to an existing");
                    }
                    break;
                case SaveButtonActionCommand:
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setMultiSelectionEnabled(false);
                    if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        try {
                            PrintWriter out = new PrintWriter(file);
                            out.println(chatLogTextPane.getText());
                            out.close();
                            JOptionPane.showMessageDialog(null, "The file was saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Error when saving the file", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                case ClearButtonActionCommand:
                    clearLog();
                    break;
                default:
                    break;
            }
        }
    }

    public class GUIFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            JTextField textField = (JTextField)e.getComponent();
            String placeholder = "";
            if (textField == hostTextField) {
                placeholder = HostTextFieldPlaceholder;
            }
            else if (textField == portTextField) {
                placeholder = PortTextFieldPlaceholder;
            }
            else if (textField == nickTextField) {
                placeholder = NickTextFieldPlaceholder;
            }
            else if (textField == messageTextField) {
                placeholder = MessageTextFieldPlaceholder;
            }
            if (placeholder.length() > 0) {
                String text = textField.getText();
                if (text.equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.black);
                }
            }
            textField.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
            JTextField textField = (JTextField)e.getComponent();
            String placeholder = "";
            if (textField == hostTextField) {
                placeholder = HostTextFieldPlaceholder;
            }
            else if (textField == portTextField) {
                placeholder = PortTextFieldPlaceholder;
            }
            else if (textField == nickTextField) {
                placeholder = NickTextFieldPlaceholder;
            }
            else if (textField == messageTextField) {
                placeholder = MessageTextFieldPlaceholder;
            }
            if (placeholder.length() > 0) {
                String text = textField.getText();
                if (text.length() == 0 || text.equals(placeholder)) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.lightGray);
                }
            }
        }
    }

    private class RoomEventListener implements ChatRoomEventListener {
        public void receivedEvent(ChatRoomEvent event, String[] args) {
            switch (event) {
                case SERVER_STARTED:
                    chatLogTextPane.setText("");

                    hostButton.setText("Stop");
                    hostButton.setEnabled(true);

                    joinButton.setText("Join");
                    joinButton.setEnabled(false);

                    hostTextField.setEnabled(false);
                    portTextField.setEnabled(false);
                    nickTextField.setEnabled(false);
                    messageTextField.setEnabled(true);
                    messageTextField.requestFocusInWindow();

                    updateStatus(Status.RUNNING);
                    break;
                case FAILED_START_SERVER:
                    updateStatus(Status.DISCONNECTED);
                    if (args.length == 1) {
                        String message = args[0];
                        showErrorDialog("Server Error", message);
                    } else {
                        showErrorDialog("Server Error", "Failed to start the server");
                    }
                case SERVER_STOPPED:
                case DISCONNECTED:
                    hostButton.setText("Host");
                    hostButton.setEnabled(true);

                    joinButton.setText("Join");
                    joinButton.setEnabled(true);

                    hostTextField.setEnabled(true);
                    portTextField.setEnabled(true);
                    nickTextField.setEnabled(true);
                    messageTextField.setEnabled(false);

                    updateStatus(Status.DISCONNECTED);
                    break;
                case CHANGE_NICKNAME:
                    if (args.length == 1) {
                        String newNickname = args[0];
                        nickTextField.setText(newNickname);
                    } else {
                        System.err.println("RoomEventListener::CHANGE_NICKNAME::invalid_arguments");
                    }
                    break;
                case FAILED_STOP_SERVER:
                    showErrorDialog("Server Error", "Failed to stop the server");
                    break;
                case CONNECTED:
                    chatLogTextPane.setText("");

                    hostButton.setText("Host");
                    hostButton.setEnabled(false);

                    joinButton.setText("Disconnect");
                    joinButton.setEnabled(true);

                    hostTextField.setEnabled(false);
                    portTextField.setEnabled(false);
                    nickTextField.setEnabled(false);
                    messageTextField.setEnabled(true);

                    updateStatus(Status.CONNECTED);
                    break;
                case FAILED_CONNECT:
                    if (args.length == 1) {
                        String message = args[0];
                        showErrorDialog("Client Error", message);
                    } else {
                        showErrorDialog("Client Error", "Failed to connect to server");
                    }
                    break;
                case MESSAGE_RECEIVED:
                    if (args.length == 3) {
                        String time = args[0];
                        String from = args[1];
                        String message = args[2];
                        updateMessage(time, from, message);
                    }
                    break;
                case ERROR:
                    if (args.length == 1) {
                        String message = args[0];
                        showErrorDialog("Error", message);
                    } else {
                        showErrorDialog("Error", "Unknown Error");
                    }
                    break;
                default:
                    showErrorDialog("Error", "Unknown Event");
                    break;
            }
        }
    }

    //endregion

    //region Constants

    private static final long serialVersionUID = 7149874169210967125L;

    private static final Dimension WINDOW_SIZE = new Dimension(800, 600);
    private static final Double SIDEBAR_WIDTH = 0.3;
    private static final double CHAT_AREA_WIDTH = 0.7;
    private static final int STATUS_BAR_HEIGHT = 30;

    private static final String DefaultHostname = "localhost";
    private static final Integer DefaultPort = 1337;

    private static final String HostButtonActionCommand = "SERVER_BUTTON";
    private static final String JoinButtonActionCommand = "CLIENT_BUTTON";
    private static final String SaveButtonActionCommand = "SAVE_BUTTON";
    private static final String ClearButtonActionCommand = "CLEAR_BUTTON";
    private static final String MessageTextFieldActionCommand = "MESSAGE_TEXTFIELD";

    private static final String HostTextFieldPlaceholder = "Host-IP";
    private static final String PortTextFieldPlaceholder = "Port";
    private static final String NickTextFieldPlaceholder = "Nickname";
    private static final String MessageTextFieldPlaceholder = "Message...";

    //endregion

    //region Instance Variables

    private JTextField hostTextField;
    private JTextField portTextField;
    private JTextField nickTextField;
    private JTextField messageTextField;
    private JTextPane chatLogTextPane;
    private JButton hostButton;
    private JButton joinButton;
    private JLabel statusLabel;

    private ChatRoom room;

    //endregion

    private MainGUI() {
        super("DV1549Project-MultithreadedChat");

        addWindowListener(new GUIWindowAdapter());
        prepareGUI();
    }

    public static void main(String[] args) {
        MainGUI gui = new MainGUI();
        gui.setVisible(true);
    }

    //region GUI Preparation

    private void prepareGUI() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel sidebar = setupSideBar();
        contentPane.add(sidebar, BorderLayout.LINE_START);

        JPanel chatArea = setupChatArea();
        contentPane.add(chatArea, BorderLayout.LINE_END);

        JPanel statusBar = setupStatusBar();
        contentPane.add(statusBar, BorderLayout.PAGE_END);
    }

    private JPanel setupSideBar() {
        JPanel pane = new JPanel(null);
        int width = ((Double)(WINDOW_SIZE.width * SIDEBAR_WIDTH)).intValue();
        pane.setPreferredSize(new Dimension(width, WINDOW_SIZE.height - STATUS_BAR_HEIGHT));

        int posX = 15;
        int posY = 15;

        Dimension size = new Dimension(width - (posX*2), 40);

        hostTextField = new JTextField();
        hostTextField.setSize(size);
        hostTextField.setLocation(posX, posY);
        hostTextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        hostTextField.addFocusListener(new GUIFocusListener());
        hostTextField.setText(DefaultHostname);
        hostTextField.setEnabled(true);
        pane.add(hostTextField);

        posY += size.height + 15;
        portTextField = new JTextField();
        portTextField.setSize(size);
        portTextField.setLocation(posX, posY);
        portTextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        portTextField.addFocusListener(new GUIFocusListener());
        portTextField.setText(DefaultPort.toString());
        portTextField.setEnabled(true);
        pane.add(portTextField);

        Random rand = new Random();
        String nickname = "Client" + (rand.nextInt(998) + 1);

        posY += size.height + 15;
        nickTextField = new JTextField();
        nickTextField.setSize(size);
        nickTextField.setLocation(posX, posY);
        nickTextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        nickTextField.addFocusListener(new GUIFocusListener());
        nickTextField.setText(nickname);
        nickTextField.setEnabled(true);
        pane.add(nickTextField);

        posY += size.height + 15;
        hostButton = new JButton("Host");
        hostButton.setSize(size);
        hostButton.setLocation(posX, posY);
        hostButton.setActionCommand(HostButtonActionCommand);
        hostButton.addActionListener(new GUIActionListener());
        hostButton.setEnabled(true);
        pane.add(hostButton);

        posY += size.height + 15;
        joinButton = new JButton("Join");
        joinButton.setSize(size);
        joinButton.setLocation(posX, posY);
        joinButton.setActionCommand(JoinButtonActionCommand);
        joinButton.addActionListener(new GUIActionListener());
        joinButton.setEnabled(true);
        pane.add(joinButton);

        posY += size.height + 15;
        JButton button = new JButton("Save log");
        button.setSize(size);
        button.setLocation(posX, posY);
        button.setActionCommand(SaveButtonActionCommand);
        button.addActionListener(new GUIActionListener());
        button.setEnabled(true);
        pane.add(button);

        posY += size.height + 15;
        button = new JButton("Clear log");
        button.setSize(size);
        button.setLocation(posX, posY);
        button.setActionCommand(ClearButtonActionCommand);
        button.addActionListener(new GUIActionListener());
        button.setEnabled(true);
        pane.add(button);

        return pane;
    }

    private  JPanel setupChatArea() {
        JPanel panel = new JPanel(new BorderLayout());
        int width = ((Double)(WINDOW_SIZE.width * CHAT_AREA_WIDTH)).intValue();
        panel.setPreferredSize(new Dimension(width, WINDOW_SIZE.height - STATUS_BAR_HEIGHT));

        chatLogTextPane = new JTextPane();
        chatLogTextPane.setEditable(false);
        chatLogTextPane.setBorder(null);
        chatLogTextPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, Color.lightGray),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane chatScrollPane = new JScrollPane(chatLogTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        chatScrollPane.setBorder(null);
        panel.add(chatScrollPane, BorderLayout.CENTER);

        messageTextField = new JTextField();
        messageTextField.setPreferredSize(new Dimension(width, 50));
        messageTextField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 0, 0, Color.lightGray),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        messageTextField.setText(MessageTextFieldPlaceholder);
        messageTextField.setActionCommand(MessageTextFieldActionCommand);
        messageTextField.addActionListener(new GUIActionListener());
        messageTextField.addFocusListener(new GUIFocusListener());
        messageTextField.setEnabled(false);
        messageTextField.setForeground(Color.lightGray);
        panel.add(messageTextField, BorderLayout.PAGE_END);

        return panel;
    }

    private JPanel setupStatusBar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(WINDOW_SIZE.width, STATUS_BAR_HEIGHT));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(statusLabel);

        return panel;
    }

    //endregion

    //region GUI Handling

    private boolean validateHostnameMissingInput() {
        String hostname = hostTextField.getText();
        return (hostname.length() == 0 || hostname.equals(HostTextFieldPlaceholder));
    }

    private boolean validatePortMissingInput() {
        String port = portTextField.getText();
        return (port.length() == 0 || port.equals(PortTextFieldPlaceholder));
    }

    private boolean validatePortNumberInput() {
        int port;
        try {
            port = Integer.parseInt(portTextField.getText());
        } catch (Exception ex) {
            port = -1;
        }
        return port < 0;
    }

    private boolean validateNicknameMissingInput() {
        String nickname = nickTextField.getText();
        return (nickname.length() == 0 || nickname.equals(NickTextFieldPlaceholder));
    }

    private void updateStatus(Status status) {
        String message = "";
        switch (status) {
            case DISCONNECTED:
                message = "disconnected";
                break;
            case CONNECTED:
                message = "connected";
                break;
            case RUNNING:
                message = "running";
                break;
        }
        this.statusLabel.setText("Status: " + message);
    }

    private void updateMessage(String time, String from, String message) {
        StyledDocument doc = chatLogTextPane.getStyledDocument();
        Style style = chatLogTextPane.addStyle("I'm a Style", null);

        StyleConstants.setForeground(style, Color.black);
        try { doc.insertString(doc.getLength(), "[" + time + "]", style); }
        catch (BadLocationException e){}

        StyleConstants.setForeground(style, (from.toLowerCase().equals("server")) ? Color.red : Color.blue);
        try { doc.insertString(doc.getLength(), " <" + from + "> ", style); }
        catch (BadLocationException e){}

        StyleConstants.setForeground(style, Color.black);
        try { doc.insertString(doc.getLength(), message + "\n", style); }
        catch (BadLocationException e){}
    }

    private void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void clearLog() {
        chatLogTextPane.setText("");
    }

    //endregion

    //region Room Handling

    private ChatRoom host(int port) {
        ChatRoomServer server = new ChatRoomServer();
        server.setEventListener(new RoomEventListener());
        server.open(port);
        return server;
    }

    private ChatRoom join(String hostname, int port, String nickname) {
        ChatRoomClient client = new ChatRoomClient();
        client.setEventListener(new RoomEventListener());
        client.connect(hostname, port);
        client.sendMessage("NICK " + nickname);
        return client;
    }

    private void close() {
        if (this.room != null) {
            this.room.close();
            this.room = null;
        }
    }

    //endregion

}
