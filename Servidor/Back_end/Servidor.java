package Servidor.Back_end;

import java.io.*;
import java.net.*;

public class Servidor {
    private final int PUERTO = 1234;
    private ServerSocket ss;
    private Socket cs;
    private DataOutputStream salidaCliente;
    private BufferedReader entrada;
    private ServidorListener listener;

    public interface ServidorListener {
        void onMensajeRecibido(String mensaje);
        void onEstadoCambiado(String estado, boolean conectado);
    }

    public Servidor(ServidorListener listener) {
        this.listener = listener;
    }

    public void startServer() {
        new Thread(() -> {
            try {
                ss = new ServerSocket(PUERTO);
                notificarEstado("Iniciando servidor...\nEsperando conexión en puerto " + PUERTO + "...", false);

                cs = ss.accept();
                notificarEstado("Cliente conectado desde IP: " + cs.getInetAddress().getHostAddress(), true);

                entrada = new BufferedReader(new InputStreamReader(cs.getInputStream()));
                salidaCliente = new DataOutputStream(cs.getOutputStream());

                String mensajeCliente;
                while ((mensajeCliente = entrada.readLine()) != null) {
                    if (listener != null) {
                        listener.onMensajeRecibido("Cliente: " + mensajeCliente);
                    }
                    if (mensajeCliente.trim().equalsIgnoreCase("exit")) {
                        notificarEstado("El cliente ha finalizado la conexión.", false);
                        cerrarConexion();
                        break;
                    }
                }
            } catch (IOException e) {
                notificarEstado("Conexión finalizada o error: " + e.getMessage(), false);
            }
        }).start();
    }

    public void enviarMensaje(String mensaje) {
        if (salidaCliente != null && !mensaje.trim().isEmpty()) {
            try {
                salidaCliente.writeBytes(mensaje + "\n");
                salidaCliente.flush();
            } catch (IOException e) {
                notificarEstado("Error al enviar mensaje: " + e.getMessage(), false);
            }
        }
    }

    public void cerrarConexion() {
        try {
            if (cs != null) cs.close();
            if (ss != null) ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notificarEstado(String estado, boolean conectado) {
        if (listener != null) {
            listener.onEstadoCambiado(estado, conectado);
        }
    }
}