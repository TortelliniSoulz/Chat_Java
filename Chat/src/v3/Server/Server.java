package v3.Server;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 42069;
    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Il server e' partitos!");
        try (ServerSocket socket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(socket.accept()).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                username = in.readLine();

                synchronized (clients) {
                    if (clients.containsKey(username)) {
                        System.out.println("Username gia' utilizzato!");
                        socket.close();
                    }
                    clients.put(username, out);
                }

                broadcastMessage(username + " has joined the chat!");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/"))
                        handlePrivateMessage(message);
                    else
                        broadcastMessage(username + ": " + message);
                }
            } catch (IOException ex) {
                System.out.println(username + " disconnected!");
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                synchronized (clients) {
                    clients.remove(username);
                }
                broadcastMessage(username + " has left the chat!");
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (PrintWriter writer: clients.values()) {
                    writer.println(message);
                }
            }
        }

        private void handlePrivateMessage(String message) {
            if (!message.startsWith("/w ")) {
                out.println("Invalid command!");
                return;
            }

            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                out.println("Incorrect format! Use: /w username message");
                return;
            }

            String targetUser = parts[1];
            String privateMessage = parts[2];

            synchronized (clients) {
                if (clients.containsKey(targetUser)) {
                    clients.get(targetUser).println("whisper [" + username + "]: " + privateMessage);
                    out.println("whisper to [" + targetUser + "]: " + privateMessage);
                } else
                    out.println("Nessun utente con il nome [" + targetUser + "] trovato!");
            }
        }
    }
}
