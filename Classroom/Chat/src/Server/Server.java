package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 42069;
    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Server acceso!");

        try (ServerSocket serverSocket = new ServerSocket(PORT);) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
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

                do {
                    username = in.readLine();
                } while (checkUsername(username));

                synchronized(clients) {
                    clients.put(username, out);

                    System.out.println(username + " joined the chat!");
                    broadcastMessage(username + " joined the chat!");
                }

                // while (true) {
                //     username = in.readLine();

                //     if (username == null)
                //         return;

                //     synchronized(clients) {
                //         if (!clients.containsKey(username)) {
                //             clients.put(username, out);
                //             broadcastMessage(username + " joined the chat!");
                //             break;
                //         }
                //     }

                //     out.println("Username already taken!");
                //     out.println("Reenter your username: ");
                // }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Ricevuto: " + message);
                    
                    if (message.startsWith("/")) {
                        commandList(message.split("/")[1]);
                    } else
                        broadcastMessage(username + ": " + message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized(clients) {
                for (PrintWriter printWriter : clients.values()) {
                    printWriter.println(message);
                }
            }
        }

        private boolean checkUsername(String username) {
            boolean result = false;
            synchronized(clients) {
                if (clients.containsKey(username)) {
                    result = true;
                    out.println("Username already taken! Try again:");
                }
            }
            return result;
        }

        private void commandList(String command) {
            switch (command) {
                case "private":
                    privateMessage();
                    break;
                default:
                    break;
            }
        }

        private void privateMessage() {
            out.println("A chi vuoi scrivere? ");
            String user;

            try {
                user = in.readLine();

                synchronized(clients) {
                    if (clients.containsKey(user)) {
                        out.println("Inserisci messaggio: ");
        
                        try {
                            String message = in.readLine();
                            clients.get(user).println(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
        
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
