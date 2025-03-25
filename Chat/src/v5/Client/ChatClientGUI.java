package v5.Client;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 12345;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    
    private PrintWriter out;
    private BufferedReader in;

    public ChatClientGUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // 🔥 User List Panel
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0)); // Fixed width

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(chatScrollPane, BorderLayout.CENTER);
        frame.add(userScrollPane, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        connectToServer();
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String username = JOptionPane.showInputDialog(frame, "Enter your username:");
            out.println(username);

            // 📝 Start listening for messages
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("/users ")) {
                            updateUserList(serverMessage.substring(7));
                        } else {
                            chatArea.append(serverMessage + "\n");
                        }
                    }
                } catch (IOException e) {
                    chatArea.append("Connection lost!\n");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Cannot connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    // 🔥 Update the user list dynamically
    private void updateUserList(String userListString) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : userListString.split(",")) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user.trim());
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}