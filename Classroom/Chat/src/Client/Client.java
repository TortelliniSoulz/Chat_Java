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

        frame.getContentPane().add(chatArea, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
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
                out.println(username);
            } while ((msg = in.readLine()).equals("Username already taken! Try again:"));

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

    // public static void main(String[] args) {
    //     try (
    //         Socket socket = new Socket(SERVER_IP, PORT);
    //         Scanner scanner = new Scanner(System.in);
    //         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    //         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    //     ) {
    //         System.out.println("Inserisci username: ");
    //         String username = scanner.nextLine();
    //         out.println(username);

    //         new Thread(() -> {
    //             try {
    //                 String serverMessage;
    //                 while ((serverMessage = in.readLine()) != null)
    //                     System.out.println(serverMessage);
    //             } catch (IOException ex) {
    //                 ex.printStackTrace();
    //             }
    //         }).start();

    //         while (true) {
    //             String message = scanner.nextLine();
    //             out.println(message);
    //         }
    //     } catch (IOException ex) {
    //         ex.printStackTrace();
    //     }
    // }
}
