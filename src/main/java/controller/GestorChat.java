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
import java.util.*;
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

        ListaUsuarios lista = this.gestorXML.leerUsuarios();

        boolean existe = lista.getUsuarios().stream()
                .anyMatch(u -> u.getNombre().equalsIgnoreCase(limpio));
        if (existe) {
            throw new IllegalArgumentException("El usuario '" + limpio + "' ya existe.");
        }

        lista.getUsuarios().add(new Usuario(limpio));
        this.gestorXML.guardarUsuarios(lista);
    }

    // Fase 2: validar usuarios, crear mensaje con fecha y persistirlo.
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }

        String nombreEmisor = buscarNombreRegistrado(emisor);
        String nombreDestinatario = buscarNombreRegistrado(destinatario);

        if (nombreEmisor.equals(nombreDestinatario)) {
            throw new IllegalArgumentException("No puedes enviarte un mensaje a ti mismo.");
        }

        String fecha = LocalDateTime.now().withNano(0).toString();
        Mensaje mensaje = new Mensaje(nombreEmisor, nombreDestinatario, contenido.trim(), fecha);

        ListaMensajes lista = this.gestorXML.leerMensajes();
        lista.getMensajes().add(mensaje);
        this.gestorXML.guardarMensajes(lista);
    }

    // Fase 3: filtrar mensajes en ambos sentidos (A -> B y B -> A).
    public List<Mensaje> consultarConversacion(String usuarioA, String usuarioB) throws JAXBException {
        String nombreA = buscarNombreRegistrado(usuarioA);
        String nombreB = buscarNombreRegistrado(usuarioB);

        if (nombreA.equals(nombreB)) {
            throw new IllegalArgumentException("Debes indicar dos usuarios distintos.");
        }

        ListaMensajes lista = this.gestorXML.leerMensajes();

        return lista.getMensajes().stream()
                .filter(m -> (m.getEmisor().equals(nombreA) && m.getDestinatario().equals(nombreB))
                        || (m.getEmisor().equals(nombreB) && m.getDestinatario().equals(nombreA)))
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
        if (conversacion.isEmpty()) {
            throw new IllegalArgumentException("No hay mensajes que exportar entre esos usuarios.");
        }

        File archivo = prepararDestino(destino);

        List<String> lineas = conversacion.stream()
                .map(m -> {
                    String linea = "[" + m.getFecha().replace('T', ' ') + "] "
                            + m.getEmisor() + ": " + m.getContenido();
                    if (m.getAdjuntoNombre() != null) {
                        linea += " (adjunto: " + m.getAdjuntoNombre() + ")";
                    }
                    return linea;
                })
                .toList();

        try (BufferedWriter salida = new BufferedWriter(new FileWriter(archivo))) {
            for (String linea : lineas) {
                salida.write(linea);
                salida.newLine();
            }
        } catch (IOException e) {
            archivo.delete(); // no dejar un archivo a medias
            throw e;
        }
    }

    // Fase 5: validar extensión/tamaño, copiar con InputStream/OutputStream
    // a media/ y guardar los metadatos del adjunto en el mensaje XML.
    public void adjuntarArchivo(Mensaje mensaje, Path archivoOrigen) throws IOException, JAXBException {
        if (mensaje == null) {
            throw new IllegalArgumentException("Debes indicar un mensaje.");
        }
        if (archivoOrigen == null) {
            throw new IllegalArgumentException("Debes indicar un archivo.");
        }
        File origen = archivoOrigen.toFile();

        // 1. Validaciones del archivo
        if (!origen.exists() || !origen.isFile()) {
            throw new IOException("El archivo no existe: " + origen.getPath());
        }
        String nombre = origen.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) {
            throw new IllegalArgumentException("El archivo no tiene extensión.");
        }
        String extension = nombre.substring(punto + 1).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException("Extensión no permitida: ." + extension
                    + " (permitidas: " + String.join(", ", EXTENSIONES_PERMITIDAS) + ")");
        }
        if (origen.length() == 0) {
            throw new IllegalArgumentException("El archivo está vacío.");
        }
        if (origen.length() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo (5 MB).");
        }

        // 2. Buscar el mensaje en el historial ANTES de copiar nada
        ListaMensajes lista = this.gestorXML.leerMensajes();
        Mensaje guardado = lista.getMensajes().stream()
                .filter(m -> m.getEmisor().equals(mensaje.getEmisor())
                        && m.getDestinatario().equals(mensaje.getDestinatario())
                        && m.getFecha().equals(mensaje.getFecha())
                        && m.getContenido().equals(mensaje.getContenido()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El mensaje no existe en el historial."));

        if (guardado.getAdjuntoRuta() != null) {
            throw new IllegalArgumentException("Este mensaje ya tiene un adjunto ("
                    + guardado.getAdjuntoNombre() + ").");
        }

        // 3. Copiar a media/ con streams binarios
        File carpeta = new File("media");
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta media/.");
        }
        File destino = new File(carpeta, System.currentTimeMillis() + "_" + nombre);

        try (FileInputStream entrada = new FileInputStream(origen);
             FileOutputStream salida = new FileOutputStream(destino)) {
            byte[] bloque = new byte[1024];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                salida.write(bloque, 0, leidos);
            }
        } catch (IOException e) {
            destino.delete(); // no dejar una copia incompleta
            throw e;
        }

        // 4. Guardar los metadatos; si falla el XML, deshacer la copia
        guardado.setAdjuntoRuta(destino.getPath());
        guardado.setAdjuntoNombre(nombre);
        guardado.setAdjuntoTamano(destino.length());
        guardado.setAdjuntoTipo(extension);

        try {
            this.gestorXML.guardarMensajes(lista);
        } catch (JAXBException e) {
            destino.delete();
            throw e;
        }
    }

    // Requisito 8.4: exportar un adjunto de media/ a otra ruta.
    public void exportarAdjunto(Mensaje mensaje, Path destino) throws IOException {
        if (mensaje == null || mensaje.getAdjuntoRuta() == null) {
            throw new IllegalArgumentException("Este mensaje no tiene adjunto.");
        }

        File origen = new File(mensaje.getAdjuntoRuta());
        if (!origen.exists() || !origen.isFile()) {
            throw new IOException("El adjunto ya no está en media/: " + origen.getPath());
        }

        File copia = prepararDestino(destino);

        try (FileInputStream entrada = new FileInputStream(origen);
             FileOutputStream salida = new FileOutputStream(copia)) {
            byte[] bloque = new byte[1024];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                salida.write(bloque, 0, leidos);
            }
        } catch (IOException e) {
            copia.delete(); // no dejar una copia incompleta
            throw e;
        }
    }

    // Devuelve el nombre tal como se registró, o lanza error si no existe.
    private String buscarNombreRegistrado(String nombre) throws JAXBException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        String buscado = nombre.trim();
        return this.gestorXML.leerUsuarios().getUsuarios().stream()
                .map(Usuario::getNombre)
                .filter(n -> n.equalsIgnoreCase(buscado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario '" + buscado + "' no está registrado."));
    }

    // Comprueba el destino y crea la carpeta padre si no existe.
    private File prepararDestino(Path destino) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de destino.");
        }
        File archivo = destino.toFile();
        if (archivo.isDirectory()) {
            throw new IllegalArgumentException("El destino es una carpeta, indica un nombre de archivo.");
        }
        if (archivo.exists()) {
            throw new IllegalArgumentException("Ya existe un archivo con ese nombre: " + archivo.getPath());
        }
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta de destino: " + carpeta.getPath());
        }
        return archivo;
    }

    public List<String> listarUsuarios() throws JAXBException {
        return this.gestorXML.leerUsuarios().getUsuarios().stream()
                .map(Usuario::getNombre)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public Map<String, Long> palabrasMasComunes(String usuarioA, String usuarioB, int limite)
            throws JAXBException {
        List<Mensaje> conversacion = consultarConversacion(usuarioA, usuarioB);

        return conversacion.stream()
                .map(m -> m.getContenido().toLowerCase())
                .flatMap(c -> Arrays.stream(c.split("[^\\p{L}\\p{N}]+")))
                .filter(p -> p.length() >= 3)
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limite)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (x, y) -> x, LinkedHashMap::new));
    }
}