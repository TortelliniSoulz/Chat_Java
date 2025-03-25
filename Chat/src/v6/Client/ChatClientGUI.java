package v6.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));

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

        // 🔥 Click a username to send a private message
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null) {
                        String message = JOptionPane.showInputDialog("Send private message to " + selectedUser + ":");
                        if (message != null && !message.trim().isEmpty()) {
                            out.println("/msg " + selectedUser + " " + message);
                            chatArea.append("[Private] To " + selectedUser + ": " + message + "\n");
                        }
                    }
                }
            }
        });
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String username = JOptionPane.showInputDialog(frame, "Enter your username:");
            out.println(username);

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("/users ")) {
                            updateUserList(serverMessage.substring(7));
                        } else if (serverMessage.startsWith("[Private]")) {
                            appendMessage(serverMessage, Color.RED); // 🔥 Highlight private messages
                        } else {
                            appendMessage(serverMessage, Color.BLACK);
                        }
                    }
                } catch (IOException e) {
                    chatArea.append("⚠️ Connection lost!\n");
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

    private void appendMessage(String message, Color color) {
        chatArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}