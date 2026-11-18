package Servidor.Back_end;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor {
    private final int PUERTO = 5555;
    private ServerSocket ss;
    private ServidorListener listener;
    // Lista segura para hilos que guarda a todos los clientes conectados
    private final List<ManejadorCliente> clientesConectados = new CopyOnWriteArrayList<>();

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
                notificarEstado("Servidor multicliente iniciado en puerto " + PUERTO + ". Esperando clientes...", false);

                // Ciclo infinito para aceptar múltiples clientes
                while (!ss.isClosed()) {
                    Socket cs = ss.accept();
                    ManejadorCliente nuevoCliente = new ManejadorCliente(cs);
                    clientesConectados.add(nuevoCliente);
                    new Thread(nuevoCliente).start();

                    notificarEstado("Nuevo cliente conectado (" + cs.getInetAddress().getHostAddress() + "). Total: " + clientesConectados.size(), true);
                }
            } catch (IOException e) {
                notificarEstado("Servidor detenido: " + e.getMessage(), false);
            }
        }).start();
    }

    // Envía el mensaje del Servidor a TODOS los clientes conectados
    public void enviarMensaje(String mensaje) {
        if (!mensaje.trim().isEmpty()) {
            for (ManejadorCliente cliente : clientesConectados) {
                cliente.enviar("Servidor: " + mensaje);
            }
        }
    }

    // Reenvía el mensaje de un cliente al resto de clientes (tipo sala de chat)
    private void difundirMensaje(String mensaje, ManejadorCliente remitente) {
        for (ManejadorCliente cliente : clientesConectados) {
            if (cliente != remitente) {
                cliente.enviar(mensaje);
            }
        }
    }

    private void notificarEstado(String estado, boolean conectado) {
        if (listener != null) {
            listener.onEstadoCambiado(estado, conectado);
        }
    }

    // Clase interna que atiende a cada cliente en su propio hilo
    private class ManejadorCliente implements Runnable {
        private Socket socket;
        private DataOutputStream salida;
        private BufferedReader entrada;
        private String ipCliente;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
            this.ipCliente = socket.getInetAddress().getHostAddress();
            try {
                this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.salida = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void enviar(String msg) {
            try {
                if (salida != null) {
                    salida.writeBytes(msg + "\n");
                    salida.flush();
                }
            } catch (IOException e) {
                desconectar();
            }
        }

        @Override
        public void run() {
            String mensaje;
            try {
                while ((mensaje = entrada.readLine()) != null) {
                    String textoFormateado = "Cliente [" + ipCliente + "]: " + mensaje;
                    if (listener != null) {
                        listener.onMensajeRecibido(textoFormateado);
                    }
                    // Opcional: si quieres que los demás clientes también vean lo que escribió este cliente
                    difundirMensaje(textoFormateado, this);

                    if (mensaje.trim().equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            } catch (IOException e) {
                // El cliente cerró la ventana abruptamente
            } finally {
                desconectar();
            }
        }

        private void desconectar() {
            clientesConectados.remove(this);
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            notificarEstado("Cliente [" + ipCliente + "] desconectado. Activos: " + clientesConectados.size(), !clientesConectados.isEmpty());
        }
    }
}