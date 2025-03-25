package v6.Server;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on: " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT);
            
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    public static void updateUserList() {
        StringBuilder userList = new StringBuilder("/users ");
        synchronized (clients) {
            for (ClientHandler client : clients) {
                userList.append(client.getUsername()).append(",");
            }
        }
        broadcast(userList.toString());
    }

    public static ClientHandler getClientByUsername(String username) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.getUsername().equalsIgnoreCase(username)) {
                    return client;
                }
            }
        }
        return null;
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your username:");
                username = in.readLine().trim();
                if (username.isEmpty()) {
                    socket.close();
                    return;
                }

                System.out.println(username + " connected.");
                broadcast(username + " joined the chat!");
                updateUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/msg ")) {
                        handlePrivateMessage(message);
                    } else {
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("User " + username + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                broadcast(username + " left the chat!");
                updateUserList();
            }
        }

        private void handlePrivateMessage(String message) {
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                sendMessage("Invalid private message format! Use: /msg username message");
                return;
            }

            String recipientName = parts[1];
            String privateMessage = parts[2];

            ClientHandler recipient = getClientByUsername(recipientName);
            if (recipient != null) {
                recipient.sendMessage("[Private] " + username + ": " + privateMessage);
                sendMessage("[Private] To " + recipientName + ": " + privateMessage);
            } else {
                sendMessage("User '" + recipientName + "' not found.");
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
