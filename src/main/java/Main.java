import controller.GestorChat;
import jakarta.xml.bind.JAXBException;
import model.Mensaje;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        GestorChat chat = new GestorChat();

        int opcion;
        do {
            mostrarMenu();

            try {
                opcion = Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            try {
                switch (opcion) {
                    case 1 -> registrar(teclado, chat);
                    case 2 -> enviar(teclado, chat);
                    case 3 -> consultar(teclado, chat);
                    case 4 -> estadisticas(teclado, chat);
                    case 5 -> exportar(teclado, chat);
                    case 6 -> adjuntar(teclado, chat);
                    case 7 -> exportarAdjunto(teclado, chat);
                    case 0 -> System.out.println("Hasta pronto.");
                    default -> System.out.println("Opción no válida. Elige un número del 0 al 7.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: " + e.getMessage());
            } catch (JAXBException e) {
                System.out.println("Error con el XML: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e);
            }
        } while (opcion != 0);

        teclado.close();
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("=== Chat Offline - Proyecto UD1 ===");
        System.out.println("1. Registrar usuario");
        System.out.println("2. Enviar mensaje");
        System.out.println("3. Consultar conversación");
        System.out.println("4. Estadísticas");
        System.out.println("5. Exportar conversación");
        System.out.println("6. Adjuntar archivo");
        System.out.println("7. Exportar un adjunto");
        System.out.println("0. Salir");
        System.out.print("Elige una opción: ");
    }

    // Repite la pregunta hasta que se escriba algo.
    private static String pedir(Scanner teclado, String texto) {
        String valor;
        do {
            System.out.print(texto);
            valor = teclado.nextLine().trim();
            if (valor.isEmpty()) {
                System.out.println("No puede estar vacío.");
            }
        } while (valor.isEmpty());
        return valor;
    }

    // Pide una ruta y comprueba que sea válida.
    private static Path pedirRuta(Scanner teclado, String texto) {
        while (true) {
            String ruta = pedir(teclado, texto);
            try {
                return Path.of(ruta);
            } catch (InvalidPathException e) {
                System.out.println("La ruta no es válida.");
            }
        }
    }

    // Pide un número entre 1 y max; devuelve -1 si no es válido.
    private static int pedirNumero(Scanner teclado, String texto, int max) {
        String entrada = pedir(teclado, texto);
        try {
            int numero = Integer.parseInt(entrada);
            if (numero < 1 || numero > max) {
                System.out.println("Debes elegir un número entre 1 y " + max + ".");
                return -1;
            }
            return numero;
        } catch (NumberFormatException e) {
            System.out.println("Número no válido.");
            return -1;
        }
    }

    private static String formatearTamano(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        }
        return String.format("%.1f KB", bytes / 1024.0);
    }

    private static void registrar(Scanner teclado, GestorChat chat) throws JAXBException {
        String nombre = pedir(teclado, "Nombre del nuevo usuario: ");
        chat.registrarUsuario(nombre);
        System.out.println("Usuario '" + nombre + "' registrado correctamente.");
    }

    private static void enviar(Scanner teclado, GestorChat chat) throws JAXBException {
        String emisor = pedir(teclado, "Emisor: ");
        String destinatario = pedir(teclado, "Destinatario: ");
        String contenido = pedir(teclado, "Mensaje: ");
        chat.enviarMensaje(emisor, destinatario, contenido);
        System.out.println("Mensaje enviado.");
    }

    private static void consultar(Scanner teclado, GestorChat chat) throws JAXBException {
        String a = pedir(teclado, "Usuario A: ");
        String b = pedir(teclado, "Usuario B: ");

        List<Mensaje> conversacion = chat.consultarConversacion(a, b);
        if (conversacion.isEmpty()) {
            System.out.println("Todavía no hay mensajes entre " + a + " y " + b + ".");
            return;
        }

        System.out.println();
        System.out.println("========== " + a + " <-> " + b + " (" + conversacion.size() + " mensajes) ==========");
        for (Mensaje m : conversacion) {
            System.out.println("[" + m.getFecha().replace('T', ' ') + "] " + m.getEmisor() + ": " + m.getContenido());
            if (m.getAdjuntoRuta() != null) {
                File adjunto = new File(m.getAdjuntoRuta());
                if (adjunto.exists()) {
                    System.out.println("      Adjunto: " + m.getAdjuntoNombre()
                            + " (" + formatearTamano(m.getAdjuntoTamano()) + ", " + m.getAdjuntoTipo() + ")");
                } else {
                    System.out.println("      Adjunto: " + m.getAdjuntoNombre() + " [ARCHIVO NO ENCONTRADO]");
                }
            }
        }
        System.out.println("=====================================================");
    }

    private static void estadisticas(Scanner teclado, GestorChat chat) throws JAXBException {
        String a = pedir(teclado, "Usuario A: ");
        String b = pedir(teclado, "Usuario B: ");

        Map<String, Long> datos = chat.estadisticas(a, b);
        if (datos.isEmpty()) {
            System.out.println("Todavía no hay mensajes entre " + a + " y " + b + ".");
            return;
        }

        long total = datos.values().stream().mapToLong(Long::longValue).sum();

        System.out.println();
        System.out.println("--- ESTADÍSTICAS ---");
        datos.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.printf("%s: %d mensajes (%.0f%%)%n",
                        e.getKey(), e.getValue(), e.getValue() * 100.0 / total));
        System.out.println("Total: " + total + " mensajes");
    }

    private static void exportar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        String a = pedir(teclado, "Usuario A: ");
        String b = pedir(teclado, "Usuario B: ");
        Path ruta = pedirRuta(teclado, "Archivo de destino (ej. conversacion.txt): ");

        chat.exportarConversacion(a, b, ruta);
        System.out.println("Conversación exportada a " + ruta);
    }

    private static void adjuntar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        String a = pedir(teclado, "Usuario A: ");
        String b = pedir(teclado, "Usuario B: ");

        List<Mensaje> conversacion = chat.consultarConversacion(a, b);
        if (conversacion.isEmpty()) {
            System.out.println("Todavía no hay mensajes entre " + a + " y " + b + ".");
            return;
        }

        for (int i = 0; i < conversacion.size(); i++) {
            Mensaje m = conversacion.get(i);
            System.out.println((i + 1) + ". [" + m.getFecha().replace('T', ' ') + "] "
                    + m.getEmisor() + ": " + m.getContenido()
                    + (m.getAdjuntoRuta() != null ? "  (ya tiene adjunto)" : ""));
        }

        int numero = pedirNumero(teclado, "Número del mensaje al que adjuntar: ", conversacion.size());
        if (numero == -1) {
            return;
        }
        Path ruta = pedirRuta(teclado, "Ruta del archivo a adjuntar: ");

        chat.adjuntarArchivo(conversacion.get(numero - 1), ruta);
        System.out.println("Archivo adjuntado correctamente.");
    }

    private static void exportarAdjunto(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        String a = pedir(teclado, "Usuario A: ");
        String b = pedir(teclado, "Usuario B: ");

        List<Mensaje> conAdjunto = chat.consultarConversacion(a, b).stream()
                .filter(m -> m.getAdjuntoRuta() != null)
                .toList();

        if (conAdjunto.isEmpty()) {
            System.out.println("No hay mensajes con adjunto en esta conversación.");
            return;
        }

        for (int i = 0; i < conAdjunto.size(); i++) {
            Mensaje m = conAdjunto.get(i);
            boolean existe = new File(m.getAdjuntoRuta()).exists();
            System.out.println((i + 1) + ". [" + m.getFecha().replace('T', ' ') + "] " + m.getEmisor()
                    + ": " + m.getAdjuntoNombre() + (existe ? "" : " [ARCHIVO NO ENCONTRADO]"));
        }

        int numero = pedirNumero(teclado, "Número del adjunto a exportar: ", conAdjunto.size());
        if (numero == -1) {
            return;
        }
        Path ruta = pedirRuta(teclado, "Ruta de destino (ej. copia.png): ");

        chat.exportarAdjunto(conAdjunto.get(numero - 1), ruta);
        System.out.println("Adjunto exportado a " + ruta);
    }
}