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

    public ThreadServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            while (true) {
                String action = (String) in.readObject();

                if (action.equals("UPLOAD")) {
                    // Recibir y agregar imagen a la carpeta "uploaded_images"
                    byte[] imageBytes = (byte[]) in.readObject();
                    String imageName = saveImage(imageBytes, "src/uploaded_images");
                    Server.addToGallery(imageName);
                    out.writeObject("Imagen subida exitosamente: " + imageName);
                } else if (action.equals("DOWNLOAD")) {
                    // Enviar la galería al cliente
                    sendImagesToClient(out, "src/uploaded_images");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
