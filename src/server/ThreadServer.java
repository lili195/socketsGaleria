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
        System.out.println("Cliente conectado");
        try (
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            while (!Thread.currentThread().isInterrupted()) {
                String action = (String) in.readObject();

                if (action.equals("UPLOAD")) {
                    // Recibir y agregar imagen a la carpeta "uploaded_images"
                    System.out.println("Iniciando proceso de recepcion de imagen");
                    byte[] imageBytes = (byte[]) in.readObject();
                    String imageName = saveImage(imageBytes, "src/uploaded_images");
                    Server.addToGallery(imageName);
                    System.out.println("Imagen subida exitosamente: " + imageName);
                    out.writeObject("Imagen subida exitosamente: " + imageName);
                } else if (action.equals("DOWNLOAD")) {
                    // Enviar la galería al cliente
                    sendImagesToClient(out, "src/uploaded_images");
                } else if (action.equals("EXIT")) {
                    out.writeObject("Gracias por usar esta app :D ");
                    System.out.println("Cliente desconectado");
                    this.interrupt();
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
            System.out.println("Proceso de envio de imagenes al cliente finalizado con exito");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
}
