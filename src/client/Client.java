package client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
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
        try (  Socket socket = new Socket(serverAddress, port);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            boolean running = true;
            while (running) {
                printMenu();
                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        out.writeObject("UPLOAD");
                        System.out.print("Ingrese la ruta de la imagen: ");
                        String imagePath = scanner.next();
                        handleOption1(out, in, imagePath);
                        break;
                    case 2:
                        out.writeObject("DOWNLOAD");
                        String imageName;
                        while ((imageName = (String) in.readObject()) != null && !imageName.equals("FINISHED")) {
                            byte[] imageBytes = (byte[]) in.readObject();
                            File galleryFolder = new File(GALLERY_FOLDER);
                            if (!galleryFolder.exists()) {
                                createGalleryFolderIfNotExists(galleryFolder);
                            }
                            String imgPath = Paths.get(GALLERY_FOLDER, imageName).toString();
                            saveImage(imageBytes, imgPath);
                        }
                        System.out.println("Descarga completa.");
                        break;
                    case 3:
                        out.writeObject("EXIT");
                        System.out.println(in.readObject());
                        socket.close();
                        running = false;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void printMenu() {
        System.out.println("1. Subir imagen");
        System.out.println("2. Descargar galería");
        System.out.println("3. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static String handleOption1(ObjectOutputStream out, ObjectInputStream in, String imagePath) {
        String response = "";
        try {
            byte[] imageBytes = readImage(imagePath);
            out.writeObject(imageBytes);
            response = (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static void createGalleryFolderIfNotExists(File galleryFolder) {
        try {
            Files.createDirectories(galleryFolder.toPath());
            System.out.println("Carpeta 'galery' creada exitosamente.");
        } catch (IOException e) {
            System.err.println("Error creando la carpeta 'galery': " + e.getMessage());
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
