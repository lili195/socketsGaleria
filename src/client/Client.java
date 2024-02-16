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
    private static String serverAddress;
    private static int port;
    private static final String GALLERY_FOLDER = "src/galery";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese la dirección del servidor: ");
        serverAddress = scanner.nextLine();

        System.out.print("Ingrese el puerto del servidor: ");
        port = scanner.nextInt();

        try (
                Socket socket = new Socket(serverAddress, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            while (true) {
                System.out.println("1. Subir imagen");
                System.out.println("2. Descargar galería");
                System.out.println("3. Salir");
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
                } else if (option == 3) {
                    out.writeObject("EXIT");
                    System.out.println(in.readObject());
                    socket.close();
                    return;
                } else {
                    System.out.println("Opción no válida.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
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
