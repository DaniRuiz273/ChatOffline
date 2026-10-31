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

/**
 * Interfaz de consola del chat
 */
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

    /**
     * Método que pide un texto por teclado y repite la pregunta mientras esté vacío
     * @param teclado Scanner con el que se lee la entrada
     * @param texto Pregunta que se muestra al usuario
     * @return El texto escrito, sin espacios al principio ni al final
     */
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

    /**
     * Método que pide una ruta por teclado y repite la pregunta si tiene caracteres no válidos
     * @param teclado Scanner con el que se lee la entrada
     * @param texto Pregunta que se muestra al usuario
     * @return La ruta ya convertida a Path
     */
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

    /**
     * Método que pide un número y comprueba que esté entre 1 y el máximo indicado
     * @param teclado Scanner con el que se lee la entrada
     * @param texto Pregunta que se muestra al usuario
     * @param max Número más alto que se acepta
     * @return El número elegido, o -1 si no es válido
     */
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

    /**
     * Método que convierte un tamaño en bytes a un texto legible (bytes o KB)
     * @param bytes Tamaño del archivo en bytes
     * @return El tamaño como texto, por ejemplo "20,0 KB"
     */
    private static String formatearTamano(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        }
        return String.format("%.1f KB", bytes / 1024.0);
    }

    /**
     * Opción 1: pide un nombre y registra un usuario nuevo
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer o guardar usuarios.xml
     */
    private static void registrar(Scanner teclado, GestorChat chat) throws JAXBException {
        String nombre = pedir(teclado, "Nombre del nuevo usuario: ");
        chat.registrarUsuario(nombre);
        System.out.println("Usuario '" + nombre + "' registrado correctamente.");
    }

    /**
     * Opción 2: pide emisor, destinatario y texto, y envía el mensaje
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer o guardar algún XML
     */
    private static void enviar(Scanner teclado, GestorChat chat) throws JAXBException {
        mostrarUsuarios(chat);
        String emisor = pedir(teclado, "Emisor: ");
        String destinatario = pedir(teclado, "Destinatario: ");
        String contenido = pedir(teclado, "Mensaje: ");
        chat.enviarMensaje(emisor, destinatario, contenido);
        System.out.println("Mensaje enviado.");
    }

    /**
     * Opción 3: muestra la conversación entre dos usuarios. Si un adjunto ya no
     * está en media/, lo marca como [ARCHIVO NO ENCONTRADO] sin borrar el mensaje
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer algún XML
     */
    private static void consultar(Scanner teclado, GestorChat chat) throws JAXBException {
        mostrarUsuarios(chat);
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

    /**
     * Opción 4: muestra los mensajes de cada usuario con su porcentaje
     * y las palabras más usadas de la conversación
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer algún XML
     */
    private static void estadisticas(Scanner teclado, GestorChat chat) throws JAXBException {
        mostrarUsuarios(chat);
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

        Map<String, Long> palabras = chat.palabrasMasComunes(a, b, 5);
        if (!palabras.isEmpty()) {
            System.out.println("Palabras más usadas:");
            palabras.forEach((palabra, veces) ->
                    System.out.println("  " + palabra + ": " + veces + (veces == 1 ? " vez" : " veces")));
        }
    }

    /**
     * Opción 5: exporta una conversación a un archivo de texto
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer algún XML
     * @throws IOException si falla la escritura del archivo
     */
    private static void exportar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        mostrarUsuarios(chat);
        String a = pedir(teclado, "Usuario A: ");
        String b = pedir(teclado, "Usuario B: ");
        Path ruta = pedirRuta(teclado, "Archivo de destino (ej. conversacion.txt): ");

        chat.exportarConversacion(a, b, ruta);
        System.out.println("Conversación exportada a " + ruta);
    }

    /**
     * Opción 6: muestra la conversación numerada y adjunta un archivo al mensaje elegido
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer o guardar mensajes.xml
     * @throws IOException si el archivo no existe o falla la copia
     */
    private static void adjuntar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        mostrarUsuarios(chat);
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

    /**
     * Opción 7: lista los mensajes con adjunto de una conversación y copia
     * el elegido desde media/ a la ruta indicada
     * @param teclado Scanner con el que se lee la entrada
     * @param chat Gestor que aplica las reglas del chat
     * @throws JAXBException si no se puede leer algún XML
     * @throws IOException si el adjunto no existe o falla la copia
     */
    private static void exportarAdjunto(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        mostrarUsuarios(chat);
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

    /**
     * Método que muestra los usuarios registrados para no tener que recordarlos.
     * Si no hay ninguno, lanza un aviso que el main muestra al usuario
     * @param chat Gestor del que se obtiene la lista de usuarios
     * @throws JAXBException si no se puede leer usuarios.xml
     */
    private static void mostrarUsuarios(GestorChat chat) throws JAXBException {
        List<String> usuarios = chat.listarUsuarios();
        if (usuarios.isEmpty()) {
            throw new IllegalArgumentException("No hay usuarios registrados. Usa la opción 1 primero.");
        }
        System.out.println("Usuarios registrados: " + String.join(", ", usuarios));
    }
}