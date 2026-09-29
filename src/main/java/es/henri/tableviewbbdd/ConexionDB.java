package es.henri.tableviewbbdd;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * Conexión con la base de datos.
 * Los datos de conexión (host, puerto, base de datos, usuario y contraseña)
 * se leen del archivo .env de la raíz del proyecto, que está en .gitignore:
 *
 * <pre>
 * DB_HOST=localhost
 * DB_PORT=3307
 * DB_NAME=DEIN
 * DB_USER=root
 * DB_PASSWORD=...
 * </pre>
 */
public class ConexionDB implements AutoCloseable {
    private Connection conexion;

    public ConexionDB() throws SQLException {
        DotenvBuilder configuracion = Dotenv.configure()
                .filename(".env")
                .ignoreIfMissing()
                .ignoreIfMalformed();
        String directorio = buscarDirectorioConEnv();
        if (directorio != null) {
            configuracion.directory(directorio);
        }
        Dotenv dotenv = configuracion.load();

        String host = valor(dotenv, "DB_HOST");
        String puerto = valor(dotenv, "DB_PORT");
        String baseDatos = valor(dotenv, "DB_NAME");
        String usuario = valor(dotenv, "DB_USER");
        String password = valor(dotenv, "DB_PASSWORD");

        String cadenaConexion = "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?serverTimezone=" + TimeZone.getDefault().getID();
        conexion = DriverManager.getConnection(cadenaConexion, usuario, password);
        conexion.setAutoCommit(true);
    }

    public Connection getConexion() {
        return conexion;
    }

    /**
     * Busca el directorio que contiene el archivo .env subiendo desde
     * el directorio de trabajo actual. Así funciona aunque la aplicación
     * se lance desde otra carpeta.
     *
     * @return la ruta del directorio que contiene .env, o null si no aparece
     */
    private String buscarDirectorioConEnv() {
        File directorio = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 6 && directorio != null; i++) {
            if (new File(directorio, ".env").isFile()) {
                return directorio.getAbsolutePath();
            }
            directorio = directorio.getParentFile();
        }
        return null;
    }

    /**
     * Lee una variable obligatoria del archivo .env.
     *
     * @param dotenv entorno cargado
     * @param clave  nombre de la variable
     * @return el valor de la variable
     * @throws SQLException si la variable no existe o está vacía
     */
    private String valor(Dotenv dotenv, String clave) throws SQLException {
        String valor = dotenv.get(clave);
        if (valor == null || valor.trim().isEmpty()) {
            throw new SQLException(
                    "Falta la variable " + clave + " en el archivo .env de la raíz del proyecto");
        }
        return valor.trim();
    }

    @Override
    public void close() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
