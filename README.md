# Chat Offline con gestión de conversaciones en XML

Proyecto de la Unidad 1 de **Acceso a Datos** (2.º DAM, IES Francisco de los Ríos).

Aplicación de consola en Java que simula un chat sin conexión. Los usuarios y los mensajes se guardan en ficheros XML con **JAXB**, las consultas y estadísticas se resuelven con **Stream API** y los archivos adjuntos se copian a la carpeta `media/` con **InputStream/OutputStream**.

## Funcionalidades

| Opción | Qué hace |
|---|---|
| 1. Registrar usuario | Guarda un usuario nuevo en `usuarios.xml`. No admite nombres vacíos ni repetidos (sin distinguir mayúsculas). |
| 2. Enviar mensaje | Añade un mensaje al historial `mensajes.xml` con la fecha actual. Los mensajes no se pueden borrar. |
| 3. Consultar conversación | Muestra los mensajes entre dos usuarios, en los dos sentidos y ordenados por fecha. Marca los adjuntos que ya no existen. |
| 4. Estadísticas | Mensajes por usuario (con porcentaje), total y palabras más usadas. |
| 5. Exportar conversación | Guarda la conversación en un archivo de texto. |
| 6. Adjuntar archivo | Valida extensión y tamaño, copia el archivo a `media/` y guarda sus datos en el mensaje. |
| 7. Exportar un adjunto | Copia un adjunto desde `media/` a la ruta indicada. |
| 0. Salir | Cierra el programa. |

Antes de pedir un usuario, el menú muestra la lista de usuarios registrados.

## Requisitos

- Java 17 o superior (se usan `Stream.toList()` y `switch` con flechas).
- Maven.
- Dependencias (ya incluidas en el `pom.xml`):
  - `jakarta.xml.bind-api` 4.0.2
  - `jaxb-runtime` 4.0.5

## Cómo ejecutarlo

1. Clonar el repositorio y abrirlo como proyecto Maven en IntelliJ.
2. Esperar a que Maven descargue las dependencias.
3. Ejecutar la clase `Main`.

Los ficheros `usuarios.xml`, `mensajes.xml` y la carpeta `media/` se crean solos en el directorio de trabajo (la raíz del proyecto si se ejecuta desde el IDE). Están en el `.gitignore` porque son datos de ejecución.

## Estructura del proyecto

```
src/main/java
├── Main.java                  Interfaz de consola (menú)
├── controller
│   └── GestorChat.java        Reglas del chat
├── dataAccess
│   └── GestorXML.java         Lectura y escritura de XML con JAXB
└── model
    ├── Usuario.java
    ├── Mensaje.java
    ├── ListaUsuarios.java     Contenedor JAXB de usuarios
    └── ListaMensajes.java     Contenedor JAXB de mensajes
```

Las dependencias van en un solo sentido: `Main` → `GestorChat` → `GestorXML` → `model`.

## Formato de los ficheros

**usuarios.xml**

```xml
<usuarios>
    <usuario>
        <nombre>ana</nombre>
    </usuario>
</usuarios>
```

**mensajes.xml** (el mensaje de ejemplo tiene adjunto)

```xml
<mensajes>
    <mensaje>
        <emisor>luis</emisor>
        <destinatario>ana</destinatario>
        <contenido>Mira esta foto</contenido>
        <fecha>2026-10-29T13:46:10</fecha>
        <adjuntoRuta>media\1793265607909_foto.png</adjuntoRuta>
        <adjuntoNombre>foto.png</adjuntoNombre>
        <adjuntoTamano>20480</adjuntoTamano>
        <adjuntoTipo>png</adjuntoTipo>
    </mensaje>
</mensajes>
```

## Detalles técnicos

- **JAXB**: clases contenedoras (`ListaUsuarios`, `ListaMensajes`) para tener un único elemento raíz por documento. Si un XML no existe, se devuelve una lista vacía.
- **Guardado seguro**: los XML se escriben primero en un archivo auxiliar (`.tmp`) y solo se sustituye el original cuando el nuevo está completo, para no perder el historial si algo falla.
- **Stream API**: `anyMatch` (duplicados), `filter` + `sorted` (conversación), `groupingBy` + `counting` (mensajes por usuario), `flatMap` (palabras) y `map` (exportar).
- **Adjuntos**: copia por bloques de 1024 bytes con `FileInputStream` y `FileOutputStream`.
  - Extensiones permitidas: `jpg`, `jpeg`, `png`, `gif`, `pdf`, `txt`.
  - Tamaño máximo: 5 MB.
  - Si un adjunto desaparece de `media/`, la conversación lo marca como `[ARCHIVO NO ENCONTRADO]` y el mensaje se conserva.
- **Errores**: las reglas se validan en `GestorChat` y `Main` recoge todos los errores en un único punto, por lo que el programa no se cierra por una entrada incorrecta.

## Limitaciones

- La interfaz es de consola (no JavaFX).
- No se ha implementado el empaquetado en ZIP (opcional en el enunciado).
- Los mensajes no tienen identificador propio: al adjuntar se localizan por emisor, destinatario, fecha y contenido.
