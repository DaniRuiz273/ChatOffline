package model;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Usuario {
    private String nombre;

    public Usuario() { } // JAXB necesita un constructor sin argumentos.

    public Usuario(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return this.nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}