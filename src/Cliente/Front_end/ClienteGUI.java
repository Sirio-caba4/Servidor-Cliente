package Cliente.Front_end;

import Cliente.Back_end.Cliente;
import javax.swing.*;
import java.awt.*;

public class ClienteGUI extends JFrame implements Cliente.ClienteListener {
    private JTextArea areaConversacion;
    private JTextArea areaMensaje;
    private JButton btnEnviar;
    private Cliente backendCliente;
    private String hostIP;

    public ClienteGUI(String hostIP) {
        this.hostIP = hostIP;
        configurarVentana();
        backendCliente = new Cliente(hostIP, this);
        backendCliente.startClient();
    }

    private void configurarVentana() {
        setTitle("Chat - CLIENTE (" + hostIP + ")");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 1. Área para mostrar toda la conversación
        areaConversacion = new JTextArea();
        areaConversacion.setEditable(false);
        areaConversacion.setLineWrap(true);
        add(new JScrollPane(areaConversacion), BorderLayout.CENTER);

        // Panel inferior
        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));

        // 2. Área para escribir el texto a enviar
        areaMensaje = new JTextArea(3, 25);
        areaMensaje.setLineWrap(true);
        panelInferior.add(new JScrollPane(areaMensaje), BorderLayout.CENTER);

        // 3. Botón para la acción de enviar
        btnEnviar = new JButton("Enviar");
        btnEnviar.setEnabled(false); // Se habilita al conectar con el servidor
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);

        btnEnviar.addActionListener(e -> accionEnviar());
    }

    private void accionEnviar() {
        String texto = areaMensaje.getText().trim();
        if (!texto.isEmpty()) {
            backendCliente.enviarMensaje(texto);
            areaConversacion.append("Cliente (Yo): " + texto + "\n");
            areaMensaje.setText("");
        }
    }

    // --- Métodos que recibe el Frontend desde el Backend ---

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
            String ip = JOptionPane.showInputDialog(
                null,
                "Ingresa la dirección IP del Servidor (ej. 192.168.1.50):",
                "Configuración de Red",
                JOptionPane.QUESTION_MESSAGE
            );

            if (ip != null && !ip.trim().isEmpty()) {
                new ClienteGUI(ip.trim()).setVisible(true);
            } else {
                System.out.println("No se ingresó una IP válida.");
            }
        });
    }
}