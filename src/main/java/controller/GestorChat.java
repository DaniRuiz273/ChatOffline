package controller;

import dataAccess.GestorXML;
import jakarta.xml.bind.JAXBException;
import model.Mensaje;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class GestorChat {
    private final GestorXML gestorXML = new GestorXML();

    // TODO Fase 1: comprobar duplicados y registrar el usuario en XML.
    public void registrarUsuario(String nombre) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: registrarUsuario");
    }

    // TODO Fase 2: validar usuarios, crear mensaje con fecha y persistirlo.
    public void enviarMensaje(String emisor, String destinatario, String contenido) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: enviarMensaje");
    }

    // TODO Fase 3: filtrar mensajes en ambos sentidos (A -> B y B -> A).
    public List<Mensaje> consultarConversacion(String usuarioA, String usuarioB) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: consultarConversacion");
    }

    // TODO Fase 4: utilizar Stream API para contar mensajes por emisor.
    public Map<String, Long> estadisticas(String usuarioA, String usuarioB) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: estadisticas");
    }

    // TODO Fase 4: exportar la conversación a un archivo de texto.
    public void exportarConversacion(String usuarioA, String usuarioB, Path destino)
            throws JAXBException, IOException {
        throw new UnsupportedOperationException("Pendiente: exportarConversacion");
    }

    // TODO Fase 5: validar extensión/tamaño, copiar con InputStream/OutputStream
    // a media/ y guardar los metadatos del adjunto en el mensaje XML.
    public void adjuntarArchivo(Mensaje mensaje, Path archivoOrigen) throws IOException, JAXBException {
        throw new UnsupportedOperationException("Pendiente: adjuntarArchivo");
    }
}