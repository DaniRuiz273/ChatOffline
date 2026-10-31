package model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase contenedora de todos los usuarios registrados
 */
@XmlRootElement(name = "usuarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListaUsuarios {
    @XmlElement(name = "usuario") // Lista de usuarios
    private List<Usuario> usuarios = new ArrayList<>();

    public ListaUsuarios() { }
    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }
}