package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import server.views.ServerFrame;

public class ThreadServer extends Thread {
    private Socket clientSocket;
    private ServerFrame serverFrame;
    private static final String UPLOADS_FOLDER_PATH = "src/uploaded_images";

    public ThreadServer(Socket clientSocket, ServerFrame serverFrame) {
        this.clientSocket = clientSocket;
        this.serverFrame = serverFrame;
    }

    @Override
    public void run() {
        serverFrame.log("Cliente conectado");
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            while (!Thread.currentThread().isInterrupted()) {
                String action = (String) in.readObject();

                switch (action) {
                    case "UPLOAD":
                        serverFrame.log("Iniciando proceso de recepcion de imagen");
                        byte[] imageBytes = (byte[]) in.readObject();
                        File uploadsFolder = new File(UPLOADS_FOLDER_PATH);
                        if (!uploadsFolder.exists()) {
                            createUploadsFolderIfNotExists(uploadsFolder);
                        }
                        String imageName = saveImage(imageBytes, "src/uploaded_images");
                        ServerFrame.addToGallery(imageName);
                        serverFrame.log("¡Imagen recibida con éxito!: " + imageName);
                        out.writeObject("¡Imagen subida con éxito!: " + imageName);
                        break;
                    case "DOWNLOAD":
                        sendImagesToClient(out, "src/uploaded_images");
                        break;
                    case "EXIT":
                        out.writeObject("Gracias por usar esta app :D ");
                        serverFrame.log("Cliente desconectado");
                        this.interrupt();
                        decreaseConnectedClients();
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

    private void decreaseConnectedClients() {
        serverFrame.updateConnectedClientsLabel(
            Integer.parseInt(serverFrame.getConnectedClientsLabel().getText().split(": ")[1]) - 1
        );
    }

    private void createUploadsFolderIfNotExists(File uploadsFolder) {
        try {
            Files.createDirectories(uploadsFolder.toPath());
            serverFrame.log("Carpeta 'uploaded_images' creada exitosamente.");
        } catch (IOException e) {
            serverFrame.log("Error creando la carpeta 'uploaded_images': " + e.getMessage());
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
        serverFrame.log("Iniciando envio de imagenes al cliente");
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
            serverFrame.log("Proceso de envio de imagenes al cliente finalizado con éxito");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
