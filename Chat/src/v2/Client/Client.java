package v2.Client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 42069;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_IP, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connesso al servers!");

            System.out.println("Imposta il tuo username: ");
            String username = scanner.nextLine();
            out.println(username);

            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException ex) {
                    System.out.println("Connessione interrottas!");
                }
            }).start();

            while (true) {
                String userMessage = scanner.nextLine();
                out.println(userMessage);
            }
        } catch (IOException ex) {
            System.out.println("Errore durante la connessione al servers!");
        }
    }
}
