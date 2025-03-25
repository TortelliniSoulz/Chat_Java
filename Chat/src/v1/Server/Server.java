package v1.Server;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 42069;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

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

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Messaggio ricevuto: " + message);
                    broadcastMessage(message);
                }
            } catch (IOException ex) {
                System.out.println("Client disconnesso!");
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer: clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
