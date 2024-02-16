package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 15555;
    private static ArrayList<String> gallery = new ArrayList<>();

    
    public static ArrayList<String> getGallery() {
        return gallery;
    }

    public static void addToGallery(String image) {
        gallery.add(image);
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("server on port 15555...");
            while (true) {
                Socket connection = serverSocket.accept();
                new ThreadServer(connection).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
