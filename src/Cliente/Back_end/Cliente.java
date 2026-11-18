package Cliente.Back_end;

import java.io.*;
import java.net.*;

public class Cliente {
    private final int PUERTO = 5555;
    private String hostIP;
    private Socket cs;
    private DataOutputStream salidaServidor;
    private BufferedReader entradaServidor;
    private ClienteListener listener;

    // Interfaz para comunicarse con el Frontend
    public interface ClienteListener {
        void onMensajeRecibido(String mensaje);
        void onEstadoCambiado(String estado, boolean conectado);
    }

    public Cliente(String hostIP, ClienteListener listener) {
        this.hostIP = hostIP;
        this.listener = listener;
    }

    public void startClient() {
        new Thread(() -> {
            try {
                notificarEstado("Intentando conectar a " + hostIP + ":" + PUERTO + "...", false);
                cs = new Socket(hostIP, PUERTO);
                notificarEstado("¡Conectado al servidor exitosamente!", true);

                // Inicializar flujos bidireccionales
                salidaServidor = new DataOutputStream(cs.getOutputStream());
                entradaServidor = new BufferedReader(new InputStreamReader(cs.getInputStream()));

                // Escuchar constantemente las respuestas del servidor
                String mensajeServidor;
                while ((mensajeServidor = entradaServidor.readLine()) != null) {
                    if (listener != null) {
                        listener.onMensajeRecibido(mensajeServidor);
                    }
                }
            } catch (IOException e) {
                notificarEstado("Error en la conexión o servidor desconectado: " + e.getMessage(), false);
            }
        }).start();
    }

    public void enviarMensaje(String mensaje) {
        if (salidaServidor != null && !mensaje.trim().isEmpty()) {
            try {
                salidaServidor.writeBytes(mensaje + "\n");
                salidaServidor.flush();

                if (mensaje.trim().equalsIgnoreCase("exit")) {
                    cerrarConexion();
                    notificarEstado("Has cerrado la conexión.", false);
                }
            } catch (IOException e) {
                notificarEstado("Error al enviar mensaje: " + e.getMessage(), false);
            }
        }
    }

    public void cerrarConexion() {
        try {
            if (cs != null) cs.close();
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