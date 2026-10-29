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
        guardarSeguro(usuarios, ListaUsuarios.class, USUARIOS_XML);
    }

    public ListaUsuarios leerUsuarios() throws JAXBException {
        ListaUsuarios lista = leer(ListaUsuarios.class, USUARIOS_XML);
        return lista != null ? lista : new ListaUsuarios();
    }

    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        guardarSeguro(mensajes, ListaMensajes.class, MENSAJES_XML);
    }

    public ListaMensajes leerMensajes() throws JAXBException {
        ListaMensajes lista = leer(ListaMensajes.class, MENSAJES_XML);
        return lista != null ? lista : new ListaMensajes();
    }

    // Devuelve null si el archivo no existe.
    private <T> T leer(Class<T> clase, File archivo) throws JAXBException {
        if (!archivo.exists()) {
            return null;
        }
        try {
            JAXBContext contexto = JAXBContext.newInstance(clase);
            Unmarshaller lector = contexto.createUnmarshaller();
            return clase.cast(lector.unmarshal(archivo));
        } catch (JAXBException | ClassCastException e) {
            throw new JAXBException("El archivo " + archivo.getName()
                    + " está dañado o no tiene el formato esperado. "
                    + "Revísalo o bórralo (se guarda una copia en "
                    + archivo.getName() + ".bak cuando se modifica).", e);
        }
    }

    // Escribe en un .tmp y solo sustituye el original si todo ha ido bien.
    private void guardarSeguro(Object objeto, Class<?> clase, File destino) throws JAXBException {
        File tmp = new File(destino.getPath() + ".tmp");
        File bak = new File(destino.getPath() + ".bak");

        // 1. Escribir la nueva versión en el auxiliar
        try {
            JAXBContext contexto = JAXBContext.newInstance(clase);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(objeto, tmp);
        } catch (JAXBException e) {
            tmp.delete();
            throw e; // el original no se ha tocado
        }

        // 2. Apartar el original como copia de seguridad
        if (destino.exists()) {
            bak.delete();
            if (!destino.renameTo(bak)) {
                tmp.delete();
                throw new JAXBException("No se pudo guardar " + destino.getName()
                        + ": el archivo está en uso o no hay permisos.");
            }
        }

        // 3. Poner el auxiliar como archivo definitivo
        if (!tmp.renameTo(destino)) {
            if (bak.exists()) {
                bak.renameTo(destino); // restaurar el original
            }
            tmp.delete();
            throw new JAXBException("No se pudo guardar " + destino.getName()
                    + ". Se ha conservado la versión anterior.");
        }
        bak.delete();
    }
}