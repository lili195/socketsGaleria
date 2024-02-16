package client;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 15555;
    private static final String GALLERY_FOLDER = "src/galery";

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("1. Subir imagen");
                System.out.println("2. Descargar galería");
                System.out.print("Seleccione una opción: ");
                int option = scanner.nextInt();

                if (option == 1) {
                    out.writeObject("UPLOAD");
                    System.out.print("Ingrese la ruta de la imagen: ");
                    String imagePath = scanner.next();
                    byte[] imageBytes = readImage(imagePath);
                    out.writeObject(imageBytes);
                    String response = (String) in.readObject();
                    System.out.println(response);
                } else if (option == 2) {
                    out.writeObject("DOWNLOAD");
                    String imageName;
                    while ((imageName = (String) in.readObject()) != null && !imageName.equals("FINISHED")) {
                        byte[] imageBytes = (byte[]) in.readObject();
                        String imagePath = Paths.get(GALLERY_FOLDER, imageName).toString();
                        saveImage(imageBytes, imagePath);
                    }
                    System.out.println("Descarga completa.");
                } else {
                    System.out.println("Opción no válida.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readImage(String imagePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(imagePath)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    } 

    private static void saveImage(byte[] imageBytes, String imagePath) {
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            fos.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
