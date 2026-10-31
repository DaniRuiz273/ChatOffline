package model;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

/**
 * Representa un mensaje enviado de un usuario a otro. Los mensajes nunca se borran.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Mensaje {
    private String emisor;
    private String destinatario;
    private String contenido;
    private String fecha; // ISO-8601, por ejemplo: 2026-10-26T13:45:00
    private String adjuntoRuta; // Puede ser null si no hay adjunto.
    private String adjuntoNombre;
    private long adjuntoTamano;
    private String adjuntoTipo;

    public Mensaje() { } // JAXB necesita un constructor sin argumentos.

    public Mensaje(String emisor, String destinatario, String contenido, String fecha) {
        this.emisor = emisor;
        this.destinatario = destinatario;
        this.contenido = contenido;
        this.fecha = fecha;
    }

    public String getEmisor() { return this.emisor; }

    public void setEmisor(String emisor) { this.emisor = emisor; }

    public String getDestinatario() { return this.destinatario; }

    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getContenido() { return this.contenido; }

    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFecha() { return this.fecha; }

    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getAdjuntoRuta() { return this.adjuntoRuta; }

    public void setAdjuntoRuta(String adjuntoRuta) { this.adjuntoRuta = adjuntoRuta; }

    public String getAdjuntoNombre() { return this.adjuntoNombre; }

    public void setAdjuntoNombre(String adjuntoNombre) { this.adjuntoNombre = adjuntoNombre; }

    public long getAdjuntoTamano() { return this.adjuntoTamano; }

    public void setAdjuntoTamano(long adjuntoTamano) { this.adjuntoTamano = adjuntoTamano; }

    public String getAdjuntoTipo() { return adjuntoTipo; }

    public void setAdjuntoTipo(String adjuntoTipo) { this.adjuntoTipo = adjuntoTipo; }
}