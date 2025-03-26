package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 42069;
    private PrintWriter out;
    private BufferedReader in;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel userListModel;

    public Client() {
        frame = new JFrame("Spaiciat");
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        messageField = new JTextField("Enter message...", 30);
        messageField.setForeground(Color.GRAY);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel();
        panel.add(messageField);
        panel.add(sendButton);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JPanel userListPanel = new JPanel();
        userListPanel.add(userList);
        userListPanel.setPreferredSize(new Dimension(100, 0));

        frame.getContentPane().add(chatArea, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.getContentPane().add(userListPanel, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        chatArea.setFont(new Font("Arial", Font.BOLD, 15));

        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getForeground() == Color.GRAY) {
                    messageField.setForeground(Color.BLACK);
                    messageField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().equals("")) {
                    messageField.setForeground(Color.GRAY);
                    messageField.setText("Enter message...");
                }
            }
        });

        connectToServer();

        sendButton.addActionListener(event -> sendMessage());
        messageField.addActionListener(event -> sendMessage());
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String msg = "";
            String username;
            do {
                username = JOptionPane.showInputDialog(frame, !msg.isEmpty() ? msg : "Enter username:");
                if (username == null)
                    System.exit(0);
                else
                    out.println(username);
            } while ((msg = in.readLine()).equals("Username already taken! Try again:"));

            userListModel.addElement(username);

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        chatArea.append(serverMessage + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
