package Servidor.Front_end;

import Servidor.Back_end.Servidor;
import javax.swing.*;
import java.awt.*;

public class ServidorGUI extends JFrame implements Servidor.ServidorListener {
    private JTextArea areaConversacion;
    private JTextArea areaMensaje;
    private JButton btnEnviar;
    private Servidor backendServidor;

    public ServidorGUI() {
        configurarVentana();
        backendServidor = new Servidor(this);
        backendServidor.startServer();
    }

    private void configurarVentana() {
        setTitle("Chat - SERVIDOR");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        areaConversacion = new JTextArea();
        areaConversacion.setEditable(false);
        areaConversacion.setLineWrap(true);
        add(new JScrollPane(areaConversacion), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));

        areaMensaje = new JTextArea(3, 25);
        areaMensaje.setLineWrap(true);
        panelInferior.add(new JScrollPane(areaMensaje), BorderLayout.CENTER);

        btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(e -> accionEnviar());
    }

    private void accionEnviar() {
        String texto = areaMensaje.getText().trim();
        if (!texto.isEmpty()) {
            backendServidor.enviarMensaje(texto);
            areaConversacion.append("Servidor (Yo): " + texto + "\n");
            areaMensaje.setText("");
        }
    }

    @Override
    public void onMensajeRecibido(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            areaConversacion.append(mensaje + "\n");
        });
    }

    @Override
    public void onEstadoCambiado(String estado, boolean conectado) {
        SwingUtilities.invokeLater(() -> {
            areaConversacion.append("[SISTEMA]: " + estado + "\n");
            btnEnviar.setEnabled(conectado);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServidorGUI().setVisible(true);
        });
    }
}