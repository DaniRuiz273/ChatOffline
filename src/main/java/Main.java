import controller.GestorChat;
import jakarta.xml.bind.JAXBException;
import model.Mensaje;

import java.io.File;
import java.io.IOException;
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
                    default -> System.out.println("Opción no válida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Aviso: " + e.getMessage());
            } catch (JAXBException e) {
                System.out.println("Error con el XML: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
            }
        } while (opcion != 0);

        teclado.close();
    }

    private static void registrar(Scanner teclado, GestorChat chat) throws JAXBException {
        System.out.print("Nombre del nuevo usuario: ");
        String nombre = teclado.nextLine();
        chat.registrarUsuario(nombre);
        System.out.println("Usuario registrado correctamente.");
    }

    private static void enviar(Scanner teclado, GestorChat chat) throws JAXBException {
        System.out.print("Emisor: ");
        String emisor = teclado.nextLine().trim();
        System.out.print("Destinatario: ");
        String destinatario = teclado.nextLine().trim();
        System.out.print("Mensaje: ");
        String contenido = teclado.nextLine();
        chat.enviarMensaje(emisor, destinatario, contenido);
        System.out.println("Mensaje enviado.");
    }

    private static void consultar(Scanner teclado, GestorChat chat) throws JAXBException {
        System.out.print("Usuario A: ");
        String a = teclado.nextLine().trim();
        System.out.print("Usuario B: ");
        String b = teclado.nextLine().trim();

        List<Mensaje> conversacion = chat.consultarConversacion(a, b);
        if (conversacion.isEmpty()) {
            System.out.println("No hay mensajes entre " + a + " y " + b + ".");
            return;
        }

        System.out.println("--- CONVERSACIÓN ---");
        for (Mensaje m : conversacion) {
            System.out.println("[" + m.getFecha() + "] " + m.getEmisor() + ": " + m.getContenido());
            if (m.getAdjuntoRuta() != null) {
                File adjunto = new File(m.getAdjuntoRuta());
                if (adjunto.exists()) {
                    System.out.println("    Adjunto: " + m.getAdjuntoNombre()
                            + " (" + m.getAdjuntoTamano() + " bytes, " + m.getAdjuntoTipo() + ")");
                } else {
                    System.out.println("    Adjunto: " + m.getAdjuntoNombre()
                            + " [ARCHIVO NO ENCONTRADO]");
                }
            }
        }
    }

    private static void estadisticas(Scanner teclado, GestorChat chat) throws JAXBException {
        System.out.print("Usuario A: ");
        String a = teclado.nextLine().trim();
        System.out.print("Usuario B: ");
        String b = teclado.nextLine().trim();

        Map<String, Long> datos = chat.estadisticas(a, b);
        if (datos.isEmpty()) {
            System.out.println("No hay mensajes entre " + a + " y " + b + ".");
            return;
        }

        System.out.println("--- ESTADÍSTICAS ---");
        long total = 0;
        for (Map.Entry<String, Long> e : datos.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + " mensajes");
            total += e.getValue();
        }
        System.out.println("Total: " + total + " mensajes");
    }

    private static void exportar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        System.out.print("Usuario A: ");
        String a = teclado.nextLine().trim();
        System.out.print("Usuario B: ");
        String b = teclado.nextLine().trim();
        System.out.print("Nombre del archivo de destino (ej. conversacion.txt): ");
        String ruta = teclado.nextLine().trim();

        chat.exportarConversacion(a, b, Path.of(ruta));
        System.out.println("Conversación exportada a " + ruta);
    }

    private static void adjuntar(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        System.out.print("Usuario A: ");
        String a = teclado.nextLine().trim();
        System.out.print("Usuario B: ");
        String b = teclado.nextLine().trim();

        List<Mensaje> conversacion = chat.consultarConversacion(a, b);
        if (conversacion.isEmpty()) {
            System.out.println("No hay mensajes entre " + a + " y " + b + ".");
            return;
        }

        for (int i = 0; i < conversacion.size(); i++) {
            Mensaje m = conversacion.get(i);
            System.out.println((i + 1) + ". [" + m.getFecha() + "] "
                    + m.getEmisor() + ": " + m.getContenido());
        }

        System.out.print("Número del mensaje al que adjuntar: ");
        int numero;
        try {
            numero = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Número no válido.");
            return;
        }
        if (numero < 1 || numero > conversacion.size()) {
            System.out.println("Ese mensaje no existe.");
            return;
        }

        System.out.print("Ruta del archivo a adjuntar: ");
        String ruta = teclado.nextLine().trim();

        chat.adjuntarArchivo(conversacion.get(numero - 1), Path.of(ruta));
        System.out.println("Archivo adjuntado correctamente.");
    }

    private static void exportarAdjunto(Scanner teclado, GestorChat chat) throws JAXBException, IOException {
        System.out.print("Usuario A: ");
        String a = teclado.nextLine().trim();
        System.out.print("Usuario B: ");
        String b = teclado.nextLine().trim();

        List<Mensaje> conversacion = chat.consultarConversacion(a, b);
        List<Mensaje> conAdjunto = conversacion.stream()
                .filter(m -> m.getAdjuntoRuta() != null)
                .toList();

        if (conAdjunto.isEmpty()) {
            System.out.println("No hay mensajes con adjunto en esta conversación.");
            return;
        }

        for (int i = 0; i < conAdjunto.size(); i++) {
            Mensaje m = conAdjunto.get(i);
            System.out.println((i + 1) + ". [" + m.getFecha() + "] " + m.getEmisor()
                    + ": " + m.getAdjuntoNombre());
        }

        System.out.print("Número del adjunto a exportar: ");
        int numero;
        try {
            numero = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Número no válido.");
            return;
        }
        if (numero < 1 || numero > conAdjunto.size()) {
            System.out.println("Ese adjunto no existe.");
            return;
        }

        System.out.print("Ruta de destino (ej. copia.png): ");
        String ruta = teclado.nextLine().trim();

        chat.exportarAdjunto(conAdjunto.get(numero - 1), Path.of(ruta));
        System.out.println("Adjunto exportado a " + ruta);
    }
}