package v5.Server;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 42069;
    private static final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
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

    // 📝 Broadcast a message to all users
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    // 🔥 Update and broadcast the user list
    public static void updateUserList() {
        StringBuilder userList = new StringBuilder("/users ");
        synchronized (clients) {
            for (ClientHandler client : clients) {
                userList.append(client.getUsername()).append(",");
            }
        }
        broadcast(userList.toString());
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

                // Get username
                out.println("Enter your username:");
                username = in.readLine().trim();
                if (username.isEmpty()) {
                    socket.close();
                    return;
                }

                System.out.println(username + " connected.");
                broadcast(username + " joined the chat!");
                updateUserList(); // 🔥 Update the user list when someone joins

                // Listen for messages
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                    broadcast(username + ": " + message);
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
                updateUserList(); // 🔥 Update user list when someone leaves
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
