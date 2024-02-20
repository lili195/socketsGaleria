package server.views;

import javax.swing.*;

import server.ThreadServer;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ServerFrame extends JFrame {
    private String port;
    private JTextArea logArea;
    private JLabel connectedClientsLabel;  
    private static ArrayList<String> gallery = new ArrayList<>();
    private ServerSocket serverSocket;

    // holi

    public ServerFrame() {
        super("Server");

        while (true){
            String input = JOptionPane.showInputDialog("Ingrese el puerto del servidor:");
            if (input == null) {
                System.exit(0);
            } else {
                try {
                    int portNumber = Integer.parseInt(input);
                    if (isValidPort(portNumber)) {
                        port = input;
                        break;
                    } else {
                        JOptionPane.showMessageDialog(this, "Puerto no válido. Debe estar entre 1024 y 65535.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número válido para el puerto.");
                }
            }
        }

        

        JPanel panel = new JPanel();
        JButton closeButton = new JButton("Cerrar Servidor");

        closeButton.addActionListener(e -> handleStopServer());

        panel.add(closeButton);

        add(panel, BorderLayout.SOUTH);

        logArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

 
        connectedClientsLabel = new JLabel("Clientes conectados: 0");
        add(connectedClientsLabel, BorderLayout.NORTH);

        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        startServer();
    }

    private boolean isValidPort(int portNumber) {
        return portNumber >= 1024 && portNumber <= 65535;
    }


    public static ArrayList<String> getGallery() {
        return gallery;
    }

    public static void addToGallery(String image) {
        gallery.add(image);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(port));
            log("Server started at " + port + " " + LocalDateTime.now());
            int connectedClients = 0;

            while (true) {
                Socket connection = serverSocket.accept();
                new ThreadServer(connection, this).start();
                connectedClients++;
                updateConnectedClientsLabel(connectedClients);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateConnectedClientsLabel(int connectedClients) {
        SwingUtilities.invokeLater(() -> {
            connectedClientsLabel.setText("Clientes conectados: " + connectedClients);
        });
    }

    private void handleStopServer() {
        System.exit(0);
    }

    public JLabel getConnectedClientsLabel() {
        return connectedClientsLabel;
    }

  
}
