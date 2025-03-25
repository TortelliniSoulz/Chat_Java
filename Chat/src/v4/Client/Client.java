package v4.Client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 42069;

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;

    public Client() {
        frame = new JFrame("Chat Client");
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        messageField = new JTextField("Enter message...", 30);
        messageField.setForeground(Color.GRAY);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel();
        panel.add(messageField);
        panel.add(sendButton);

        frame.getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        connectToServer();

        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals("Enter message...")) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setText("Enter message...");
                    messageField.setForeground(Color.GRAY);
                }
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String username = JOptionPane.showInputDialog(frame, "Enter your username: ");
            out.println(username);

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        chatArea.append(serverMessage + "\n");
                    }
                } catch (IOException ex) {
                    chatArea.append("Disconnected form server!\n");
                }
            }).start();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Cannot connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            messageField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
