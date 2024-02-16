package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    private static int port;
    private static ArrayList<String> gallery = new ArrayList<>();

    
    public static ArrayList<String> getGallery() {
        return gallery;
    }

    public static void addToGallery(String image) {
        gallery.add(image);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el puerto del servidor: ");
        port = scanner.nextInt();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor activo en el puerto "+ port + "...");
            while (true) {
                Socket connection = serverSocket.accept();
                new ThreadServer(connection).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

}
