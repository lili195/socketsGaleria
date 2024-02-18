package client.views;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import java.io.IOException;
import java.net.Socket;

public class LogInFrame extends JFrame {
    private JTextField ipServerField;
    private JTextField portServerField;
    private JButton acceptButton;
    private JLabel ipServerLabel;
    private JLabel portServerLabel;
    private JLabel serverResponseLabel;

    public LogInFrame() {
        super("Conectar al servidor");
        initComponents();
        setLayout(new GridLayout(5,1));
        add(new JLabel("Bienvenido", JLabel.CENTER));
        add(createIPObtentionPanel());
        add(createPortObtentionPanel());
        add(createButtonPanel());
        add(createResponsePanel());
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        ipServerField = new JTextField(10);
        portServerField = new JTextField(10);
        acceptButton = new JButton("Aceptar");
        ipServerLabel = new JLabel("Ingrese la dirección IP del servidor");
        portServerLabel = new JLabel("Ingrese el puerto a conectar");
        serverResponseLabel = new JLabel("", JLabel.CENTER);
        configureButton();
    }

    private void configureButton() {
        acceptButton.setEnabled(false);
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipServerField.getText();
                int port = Integer.parseInt(portServerField.getText());
                serverResponseLabel.setText("Intentando conectar a " + "IP:" + ip + " Puerto:" + port);
                Socket socket = createSocketConnection(ip, port);
                if (socket.isConnected()) {
                    serverResponseLabel.setText("Conectado al servidor con éxito :D");
                    new MainClientFrame(socket);
                    setVisible(false);
                }

            }
        });

        DocumentListener documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
            
        };

        ipServerField.getDocument().addDocumentListener(documentListener);
        portServerField.getDocument().addDocumentListener(documentListener);
    }

    private void updateButtonState() {
        String ip = ipServerField.getText();
        String port = portServerField.getText();

        acceptButton.setEnabled(!ip.isEmpty() && !port.isEmpty());
    }

    private JPanel createIPObtentionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(ipServerLabel);
        panel.add(ipServerField);
        return panel;
    }

    private JPanel createPortObtentionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(portServerLabel);
        panel.add(portServerField);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(acceptButton);
        return panel;
    }

    private JPanel createResponsePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(serverResponseLabel);
        return panel;
    }

    private Socket createSocketConnection(String ipAddress, int serverPort) {
        Socket socket = null;
        try {
            socket = new Socket(ipAddress, serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }
}
