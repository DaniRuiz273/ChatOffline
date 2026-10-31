package dataAccess;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import model.ListaMensajes;
import model.ListaUsuarios;

import java.io.File;

/**
 * Capa de acceso a datos: Lee y escribe usuarios.xml y mensajes.xml con JAXB
 */
public class GestorXML {
    // Los ficheros se crean en la raíz del proyecto
    private static final File USUARIOS_XML = new File("usuarios.xml");
    private static final File MENSAJES_XML = new File("mensajes.xml");

    /**
     * Método que guarda la lista completa de usuarios en usuarios.xml
     * @param usuarios Lista de usuarios a guardar
     * @throws JAXBException si no se puede generar o guardar en XML
     */
    public void guardarUsuarios(ListaUsuarios usuarios) throws JAXBException {
        guardarSeguro(usuarios, ListaUsuarios.class, USUARIOS_XML);
    }

    /**
     * Método que lee los usuarios de usuarios.xml
     * @return La lista de usuarios o una lista vacía si el fichero aún no existe
     * @throws JAXBException si el fichero existe pero está dañado
     */
    public ListaUsuarios leerUsuarios() throws JAXBException {
        ListaUsuarios lista = leer(ListaUsuarios.class, USUARIOS_XML);
        return lista != null ? lista : new ListaUsuarios();
    }

    /**
     * Método que guarda el historial completo de mensaje en mensajes.xml
     * @param mensajes Lista de mensajes a guardar
     * @throws JAXBException si no se puede generar o guardar el XML
     */
    public void guardarMensajes(ListaMensajes mensajes) throws JAXBException {
        guardarSeguro(mensajes, ListaMensajes.class, MENSAJES_XML);
    }

    /**
     * Método para leer el historial de mensaje de mensajes.xml
     * @return La lista de mensajes o una lista vacía si el fichero aún no existe
     * @throws JAXBException si el fichero existe pero está dañado
     */
    public ListaMensajes leerMensajes() throws JAXBException {
        ListaMensajes lista = leer(ListaMensajes.class, MENSAJES_XML);
        return lista != null ? lista : new ListaMensajes();
    }

    /**
     * Método que convierte un XML en u objeto Java
     * @param clase Clase raíz del XML
     * @param archivo Fichero a leer
     * @return el objeto reconstruido, o null si el fichero no existe
     * @throws JAXBException con un mensaje claro si el XML está dañado
     */
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
                    + "Revísalo o bórralo para empezar de nuevo.", e);
        }
    }

    /**
     * Método para guardar un objeto en XML sin arriesgar el fichero original. Si se escribiera directamente sobre el original el programa fallaría a mitad
     * @param objeto Objeto a guardar (ListaUsuarios o ListaMensajes)
     * @param clase Clase raíz del objeto
     * @param destino Fichero XML definitivo
     * @throws JAXBException si no se puede guardar, en ese caso se conserva la versión anterior
     */
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
        // 4. Ha ido bien: la copia de seguridad ya no hace falta
        bak.delete();
    }
}