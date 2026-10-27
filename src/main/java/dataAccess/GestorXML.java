package dataAccess;

import jakarta.xml.bind.JAXBException;
import model.ListaMensajes;
import model.ListaUsuarios;

public class GestorXML {
    // TODO Fase 1: guardar usuarios.xml usando JAXB (Marshaller).
    public void guardarUsuarios(ListaUsuarios usuarios) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: guardarUsuarios");
    }

    // TODO Fase 1: recuperar usuarios.xml usando JAXB (Unmarshaller).
    // Si no existe el archivo, devolver una ListaUsuarios vacía.
    public ListaUsuarios leerUsuarios() throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: leerUsuarios");
    }

    // TODO Fase 2: guardar mensajes.xml usando JAXB.
    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: guardarMensajes");
    }

    // TODO Fase 2: recuperar mensajes.xml usando JAXB.
    // Si no existe el archivo, devolver una ListaMensajes vacía.
    public ListaMensajes leerMensajes() throws JAXBException {
        throw new UnsupportedOperationException("Pendiente: leerMensajes");
    }
}