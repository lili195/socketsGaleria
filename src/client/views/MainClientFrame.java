package client.views;

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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class MainClientFrame extends JFrame {
    private static final String GALLERY_FOLDER = "src/galery";
    private JButton uploadImagesButton;
    private JButton downloadGalleryButton;
    private JButton exitButton;

    public MainClientFrame(Socket socket) {
        super("Galería de imágenes");
        initComponents(socket);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents(Socket socket) {
        setLayout(new FlowLayout());
        uploadImagesButton = new JButton("Subir Imagen");
        downloadGalleryButton = new JButton("Descargar galería");
        exitButton = new JButton("Salir");
        configureButtons(socket);
        add(uploadImagesButton);
        add(downloadGalleryButton);
        add(exitButton);
    }

    private void configureButtons(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            uploadImagesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    uploadImages(socket, out, in);
                }
            });

            downloadGalleryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    downloadGallery(socket, out, in);
                }
            });

            exitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exit(socket, out, in);
                }
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Algo ocurrió: " + e.getMessage());
        }
    }

    private void uploadImages(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        try {
            out.writeObject("UPLOAD");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);

            int option = fileChooser.showOpenDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                byte[] imageBytes = readImage(selectedFile.toPath().toString());
                out.writeObject(imageBytes);
                String response = (String) in.readObject();
                JOptionPane.showMessageDialog(this, response);
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error subiendo la imagen al servidor: " + e.getMessage());
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

    private void downloadGallery(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        try {
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
            JOptionPane.showMessageDialog(this, "¡Galería descargada con éxito en la carpeta 'galería'!");
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error descargando la galería: " + e.getMessage());
        }
    }

    private void createGalleryFolderIfNotExists(File galleryFolder) {
        try {
            Files.createDirectories(galleryFolder.toPath());
            System.out.println("Carpeta 'galery' creada exitosamente.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error creando la carpeta 'galery': " + e.getMessage());
        }

    }

    private void saveImage(byte[] imageBytes, String imagePath) {
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            fos.write(imageBytes);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error guardando las imagenes: " + e.getMessage());
        }
    }

    private void exit(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        int option = JOptionPane.showConfirmDialog(this, "¿Está seguro de que quiere salir?", "Exit",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                out.writeObject("EXIT");
                System.out.println(in.readObject());
                socket.close();
                System.exit(0);
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error cerrando la app " + e.getMessage());
            }
        }
    }
}
