package es.henri.tableviewbbdd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de la tabla "personas" (columnas: id, nombre, apellido, fecha_nac).
 * Devuelve cada fila como un String[] con el mismo orden que espera
 * el TableView del controlador: [id, nombre, apellido, fecha_nac].
 */
public class PersonaDao {

    /** Consulta con las cuatro columnas de la tabla. */
    private static final String SQL_SELECT_TODAS =
            "SELECT id, nombre, apellido, fecha_nac FROM personas";

    /** Mayor id numérico existente; NULL o 0 si la tabla está vacía. */
    private static final String SQL_MAX_ID =
            "SELECT MAX(CAST(id AS UNSIGNED)) FROM personas";

    /** Inserción de una persona con el id calculado por el DAO. */
    private static final String SQL_INSERT =
            "INSERT INTO personas (id, nombre, apellido, fecha_nac) VALUES (?, ?, ?, ?)";

    /** Borrado de una persona por su id. */
    private static final String SQL_DELETE = "DELETE FROM personas WHERE id = ?";

    /**
     * Recupera todas las personas de la BBDD.
     *
     * @return lista de filas [id, nombre, apellido, fecha_nac]
     * @throws SQLException si falla la conexión o la consulta
     */
    public List<String[]> obtenerTodas() throws SQLException {
        List<String[]> personas = new ArrayList<>();

        try (ConexionDB db = new ConexionDB();
             Connection con = db.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL_SELECT_TODAS)) {

            while (rs.next()) {
                personas.add(new String[]{
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("fecha_nac")
                });
            }
        }
        return personas;
    }

    /**
     * Inserta una persona en la BBDD. Como la tabla no tiene
     * AUTO_INCREMENT, el id se calcula como el mayor id existente + 1.
     *
     * @param nombre    nombre de la persona
     * @param apellido  apellido de la persona
     * @param fechaNac  fecha en formato yyyy-MM-dd, o null si no hay fecha
     * @return el id asignado a la fila insertada
     * @throws SQLException si falla la conexión o la inserción
     */
    public String insertar(String nombre, String apellido, String fechaNac) throws SQLException {
        String id;

        try (ConexionDB db = new ConexionDB();
             Connection con = db.getConexion()) {

            id = String.valueOf(siguienteId(con));

            try (PreparedStatement ps = con.prepareStatement(SQL_INSERT)) {
                ps.setString(1, id);
                ps.setString(2, nombre);
                ps.setString(3, apellido);
                if (fechaNac == null || fechaNac.isEmpty()) {
                    ps.setNull(4, Types.VARCHAR);
                } else {
                    ps.setString(4, fechaNac);
                }
                ps.executeUpdate();
            }
        }
        return id;
    }

    /**
     * Calcula el siguiente id libre: mayor id numérico de la tabla + 1,
     * empezando por 1 si la tabla no tiene filas.
     *
     * @param con conexión activa a la BBDD
     * @return siguiente id a usar
     * @throws SQLException si falla la consulta
     */
    private int siguienteId(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL_MAX_ID)) {
            return rs.next() ? rs.getInt(1) + 1 : 1;
        }
    }

    /**
     * Elimina de la BBDD la persona con el id indicado.
     *
     * @param id identificador de la persona a borrar
     * @throws SQLException si falla la conexión o el borrado
     */
    public void borrar(String id) throws SQLException {
        try (ConexionDB db = new ConexionDB();
             Connection con = db.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public void reiniciar() throws SQLException{
        try (ConexionDB db = new ConexionDB();
             Connection con = db.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM personas WHERE 1=1")) {

            ps.executeUpdate();
        }
    }
}
