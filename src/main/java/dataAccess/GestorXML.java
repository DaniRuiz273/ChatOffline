package dataAccess;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.ListaMensajes;
import model.ListaUsuarios;

import java.io.File;

public class GestorXML {
    private static final File USUARIOS_XML = new File("usuarios.xml");
    private static final File MENSAJES_XML = new File("mensajes.xml");


    public void guardarUsuarios(ListaUsuarios usuarios) throws JAXBException {
        JAXBContext contexto = JAXBContext.newInstance(ListaUsuarios.class);
        Marshaller marshaller = contexto.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(usuarios, USUARIOS_XML);
    }

    public ListaUsuarios leerUsuarios() throws JAXBException {
        if (!USUARIOS_XML.exists()) {
            return new ListaUsuarios();
        }
        JAXBContext contexto = JAXBContext.newInstance(ListaUsuarios.class);
        Unmarshaller lector = contexto.createUnmarshaller();
        return (ListaUsuarios) lector.unmarshal(USUARIOS_XML);
    }

    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        JAXBContext contexto = JAXBContext.newInstance(ListaMensajes.class);
        Marshaller marshaller = contexto.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(mensajes, MENSAJES_XML);
    }

    // Si no existe el archivo, devolver una ListaMensajes vacía.
    public ListaMensajes leerMensajes() throws JAXBException {
        if (!MENSAJES_XML.exists()) {
            return new ListaMensajes();
        }
        JAXBContext contexto = JAXBContext.newInstance(ListaMensajes.class);
        Unmarshaller lector = contexto.createUnmarshaller();
        return (ListaMensajes) lector.unmarshal(MENSAJES_XML);
    }
}