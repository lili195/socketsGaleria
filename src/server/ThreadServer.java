package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ThreadServer extends Thread {
    private Socket clientSocket;
    private static final String UPLOADS_FOLDER_PATH = "src/uploaded_images";

    public ThreadServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Cliente conectado");
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            while (!Thread.currentThread().isInterrupted()) {
                String action = (String) in.readObject();

                switch (action) {
                    case "UPLOAD":
                        System.out.println("Iniciando proceso de recepcion de imagen");
                        byte[] imageBytes = (byte[]) in.readObject();
                        File uploadsFolder = new File(UPLOADS_FOLDER_PATH);
                        if (!uploadsFolder.exists()) {
                            createUploadsFolderIfNotExists(uploadsFolder);
                        }
                        String imageName = saveImage(imageBytes, "src/uploaded_images");
                        Server.addToGallery(imageName);
                        System.out.println("¡Imagen recibida con éxito!: " + imageName);
                        out.writeObject("¡Imagen subida con éxito!: " + imageName);
                        break;
                    case "DOWNLOAD":
                        sendImagesToClient(out, "src/uploaded_images");
                        break;
                    case "EXIT":
                        out.writeObject("Gracias por usar esta app :D ");
                        System.out.println("Cliente desconectado");
                        this.interrupt();
                        break;
                    default:
                        out.writeObject("Acción no válida");
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void createUploadsFolderIfNotExists(File uploadsFolder) {
        try {
            Files.createDirectories(uploadsFolder.toPath());
            System.out.println("Carpeta 'uploaded_images' creada exitosamente.");
        } catch (IOException e) {
            System.err.println("Error creando la carpeta 'uploaded_images': " + e.getMessage());
        }

    }

    private String saveImage(byte[] imageBytes, String folderName) {
        String imageName = "image_" + System.currentTimeMillis() + ".jpg";
        String imagePath = Paths.get(folderName, imageName).toString();
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            fos.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageName;
    }

    private void sendImagesToClient(ObjectOutputStream out, String folderName) {
        System.out.println("Iniciando envio de imagenes al cliente");
        File folder = new File(folderName);
        File[] files = folder.listFiles();

        try {
            if (files != null && files.length > 0) {
                for (File file : files) {
                    byte[] imageBytes = Files.readAllBytes(file.toPath());
                    out.writeObject(file.getName());
                    out.writeObject(imageBytes);
                }
            }
            // Enviar el marcador "FINISHED" al final de la transmisión
            out.writeObject("FINISHED");
            System.out.println("Proceso de envio de imagenes al cliente finalizado con éxito");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
