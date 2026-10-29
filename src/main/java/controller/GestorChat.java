package controller;

import dataAccess.GestorXML;
import jakarta.xml.bind.JAXBException;
import model.ListaMensajes;
import model.ListaUsuarios;
import model.Mensaje;
import model.Usuario;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestorChat {
    private final GestorXML gestorXML = new GestorXML();

    private static final List<String> EXTENSIONES_PERMITIDAS =
            List.of("jpg", "jpeg", "png", "gif", "pdf", "txt");
    private static final long TAMANO_MAXIMO = 5L * 1024 * 1024; // 5 MB

    // Fase 1: comprobar duplicados y registrar el usuario en XML.
    public void registrarUsuario(String nombre) throws JAXBException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        String limpio = nombre.trim();

        ListaUsuarios lista = gestorXML.leerUsuarios();

        boolean existe = lista.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(limpio));
        if (existe) {
            throw new IllegalArgumentException("El usuario '" + limpio + "' ya existe.");
        }

        lista.getUsuarios().add(new Usuario(limpio));
        gestorXML.guardarUsuarios(lista);
    }

    // Fase 2: validar usuarios, crear mensaje con fecha y persistirlo.
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }

        ListaUsuarios usuarios = gestorXML.leerUsuarios();
        boolean emisorExiste = usuarios.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(emisor));
        boolean destinatarioExiste = usuarios.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(destinatario));

        if (!emisorExiste) {
            throw new IllegalArgumentException("El emisor '" + emisor + "' no está registrado.");
        }
        if (!destinatarioExiste) {
            throw new IllegalArgumentException("El destinatario '" + destinatario + "' no está registrado.");
        }

        String fecha = LocalDateTime.now().withNano(0).toString();
        Mensaje mensaje = new Mensaje(emisor, destinatario, contenido, fecha);

        ListaMensajes lista = gestorXML.leerMensajes();
        lista.getMensajes().add(mensaje);
        gestorXML.guardarMensajes(lista);
    }

    // Fase 3: filtrar mensajes en ambos sentidos (A -> B y B -> A).
    public List<Mensaje> consultarConversacion(String usuarioA, String usuarioB) throws JAXBException {
        ListaMensajes lista = gestorXML.leerMensajes();

        return lista.getMensajes().stream()
                .filter(m -> (m.getEmisor().equalsIgnoreCase(usuarioA)
                        && m.getDestinatario().equalsIgnoreCase(usuarioB))
                        || (m.getEmisor().equalsIgnoreCase(usuarioB)
                        && m.getDestinatario().equalsIgnoreCase(usuarioA)))
                .sorted(Comparator.comparing(Mensaje::getFecha))
                .toList();
    }

    // Fase 4: contar mensajes por emisor con Stream API.
    public Map<String, Long> estadisticas(String usuarioA, String usuarioB) throws JAXBException {
        List<Mensaje> conversacion = consultarConversacion(usuarioA, usuarioB);

        return conversacion.stream()
                .collect(Collectors.groupingBy(Mensaje::getEmisor, Collectors.counting()));
    }

    // Fase 4: exportar la conversación a un archivo de texto.
    public void exportarConversacion(String usuarioA, String usuarioB, Path destino)
            throws JAXBException, IOException {
        List<Mensaje> conversacion = consultarConversacion(usuarioA, usuarioB);

        List<String> lineas = conversacion.stream()
                .map(m -> "[" + m.getFecha() + "] " + m.getEmisor() + ": " + m.getContenido())
                .toList();

        try (BufferedWriter salida = new BufferedWriter(new FileWriter(destino.toFile()))) {
            for (String linea : lineas) {
                salida.write(linea);
                salida.newLine();
            }
        }
    }

    // Fase 5: validar extensión/tamaño, copiar con InputStream/OutputStream
    // a media/ y guardar los metadatos del adjunto en el mensaje XML.
    public void adjuntarArchivo(Mensaje mensaje, Path archivoOrigen) throws IOException, JAXBException {
        File origen = archivoOrigen.toFile();

        // 1. Validaciones
        if (!origen.exists() || !origen.isFile()) {
            throw new IOException("El archivo no existe: " + origen.getPath());
        }
        String nombre = origen.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto < 0) {
            throw new IllegalArgumentException("El archivo no tiene extensión.");
        }
        String extension = nombre.substring(punto + 1).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida: ." + extension);
        }
        if (origen.length() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo (5 MB).");
        }

        // 2. Copiar a media/ con streams binarios
        File carpeta = new File("media");
        carpeta.mkdirs();
        File destino = new File(carpeta, System.currentTimeMillis() + "_" + nombre);

        try (FileInputStream entrada = new FileInputStream(origen);
             FileOutputStream salida = new FileOutputStream(destino)) {
            byte[] bloque = new byte[1024];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                salida.write(bloque, 0, leidos);
            }
        }

        // 3. Guardar los metadatos en el mensaje
        mensaje.setAdjuntoRuta(destino.getPath());
        mensaje.setAdjuntoNombre(nombre);
        mensaje.setAdjuntoTamano(destino.length());
        mensaje.setAdjuntoTipo(extension);

        // 4. Actualizar ese mensaje en mensajes.xml
        ListaMensajes lista = gestorXML.leerMensajes();
        Mensaje guardado = lista.getMensajes().stream()
                .filter(m -> m.getEmisor().equals(mensaje.getEmisor())
                        && m.getDestinatario().equals(mensaje.getDestinatario())
                        && m.getFecha().equals(mensaje.getFecha())
                        && m.getContenido().equals(mensaje.getContenido()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El mensaje no existe en el historial."));

        guardado.setAdjuntoRuta(mensaje.getAdjuntoRuta());
        guardado.setAdjuntoNombre(mensaje.getAdjuntoNombre());
        guardado.setAdjuntoTamano(mensaje.getAdjuntoTamano());
        guardado.setAdjuntoTipo(mensaje.getAdjuntoTipo());
        gestorXML.guardarMensajes(lista);
    }

    // Requisito 8.4: exportar un adjunto de media/ a otra ruta.
    public void exportarAdjunto(Mensaje mensaje, Path destino) throws IOException {
        if (mensaje.getAdjuntoRuta() == null) {
            throw new IllegalArgumentException("Este mensaje no tiene adjunto.");
        }

        File origen = new File(mensaje.getAdjuntoRuta());
        if (!origen.exists() || !origen.isFile()) {
            throw new IOException("El adjunto ya no está en media/: " + origen.getPath());
        }

        File copia = destino.toFile();
        if (copia.exists()) {
            throw new IllegalArgumentException("Ya existe un archivo con ese nombre en el destino.");
        }

        try (FileInputStream entrada = new FileInputStream(origen);
             FileOutputStream salida = new FileOutputStream(copia)) {
            byte[] bloque = new byte[1024];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                salida.write(bloque, 0, leidos);
            }
        }
    }
}