package client.views;

import java.io.File;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

public class MainClientFrame extends JFrame {
    private static final String GALLERY_FOLDER = "src/galery";
    private JButton uploadImagesButton;
    private JButton downloadGalleryButton;
    private JButton exitButton;

    public MainClientFrame() {
        super("Galería de imágenes");
        initComponents();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new FlowLayout());
        uploadImagesButton = new JButton("Subir Imágenes");
        downloadGalleryButton = new JButton("Descargar galería");
        exitButton = new JButton("Salir");
        configureButtons();
        add(uploadImagesButton);
        add(downloadGalleryButton);
        add(exitButton);
    }

    private void configureButtons() {
        uploadImagesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadImages();
            }
        });

        downloadGalleryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadGallery();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });
    }

    private void uploadImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            // TODO: Implement logic to upload selected files to the server
            JOptionPane.showMessageDialog(this, "¡Imagenes subidas con éxito!");
        }
    }

    protected void downloadGallery() {
        // TODO Auto-generated method stub
        JOptionPane.showMessageDialog(this, "¡Galería descargada con éxito en la carpeta 'galería'!");
    }

    protected void exit() {
        int option = JOptionPane.showConfirmDialog(this, "¿Está seguro de que quiere salir?", "Exit", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
