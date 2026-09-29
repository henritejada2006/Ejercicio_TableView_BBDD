package es.henri.tableviewbbdd;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controlador de la vista principal.
 * Gestiona una tabla de personas usando arrays de Strings como filas:
 * [id, nombre, apellido, fecha de nacimiento].
 * Los datos se cargan, guardan y borran en la BBDD mediante {@link PersonaDao}.
 */
public class TableViewController {
    /** Botón para añadir una nueva persona a la tabla. */
    @FXML
    private Button bt_anadir;
    /** Botón para borrar la columna seleccionada de la tabla. */
    @FXML
    private Button bt_borrar;
    /** Botón para reiniciar la tabla. */
    @FXML
    private Button bt_reiniciar;
    /** Selector de fecha de nacimiento de la persona. */
    @FXML
    private DatePicker dt_fecha_nac;
    /** Etiqueta que muestra el campo "Apellido". */
    @FXML
    private Label lb_apellido;
    /** Etiqueta que muestra el campo "Fecha de nacimiento". */
    @FXML
    private Label lb_fecha_nac;
    /** Etiqueta que muestra el campo "Nombre". */
    @FXML
    private Label lb_nombre;
    /** Columna con el apellido de la persona. */
    @FXML
    private TableColumn<String[], String> tc_apellido;
    /** Columna con la fecha de nacimiento de la persona. */
    @FXML
    private TableColumn<String[], String> tc_fecha_nac;
    /** Columna con el identificador autogenerado de la persona. */
    @FXML
    private TableColumn<String[], String> tc_id;

    /** Columna con el nombre de la persona. */
    @FXML
    private TableColumn<String[], String> tc_nombre;

    /** Tabla principal que muestra las personas registradas. */
    @FXML
    private TableView<String[]> tv_main;

    /** Campo de texto para introducir el apellido. */
    @FXML
    private TextField tx_apellido;

    /** Campo de texto para introducir el nombre. */
    @FXML
    private TextField tx_nombre;

    /** Lista observable que alimenta la tabla. */
    private final ObservableList<String[]> datos = FXCollections.observableArrayList();

    /** Acceso a la BBDD de personas. */
    private final PersonaDao dao = new PersonaDao();

    /**
     * Inicializa la vista: vincula la lista observable a la tabla,
     * define cómo se extrae el valor de cada columna y carga
     * las personas almacenadas en la BBDD.
     */
    @FXML
    public void initialize() {
        tv_main.setItems(datos);

        tc_id.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[0]));
        tc_nombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[1]));
        tc_apellido.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[2]));
        tc_fecha_nac.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[3]));

        recargar();
    }

    /**
     * Vuelve a leer todas las personas de la BBDD y las
     * vuelca en la lista observable de la tabla.
     */
    private void recargar() {
        try {
            datos.setAll(dao.obtenerTodas());
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    /**
     * Inserta en la BBDD la persona indicada por los campos del formulario
     * y recarga la tabla. La fecha se envía en formato ISO (yyyy-MM-dd).
     *
     * @param event evento de ratón que desencadena la acción
     */
    @FXML
    void ClickAnadir(MouseEvent event) {
        String nombre = tx_nombre.getText().trim();
        String apellido = tx_apellido.getText().trim();
        LocalDate fechaNac = dt_fecha_nac.getValue();

        if (nombre.isEmpty() || apellido.isEmpty() || fechaNac == null) {
            mostrarAviso("Debes indicar nombre, apellido y fecha de nacimiento.");
            return;
        }

        try {
            dao.insertar(nombre, apellido, fechaNac.toString());
            recargar();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    /**
     * Elimina de la BBDD y de la tabla la fila actualmente seleccionada.
     * Si no hay ninguna selección, no hace nada.
     *
     * @param event evento de ratón que desencadena la acción
     */
    @FXML
    void ClickBorrar(MouseEvent event) {
        String[] sel = tv_main.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            dao.borrar(sel[0]);
            datos.remove(sel);
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    /**
     * Vacía la tabla y borra todos los campos del formulario.
     * No modifica los datos de la BBDD.
     *
     * @param event evento de ratón que desencadena la acción
     */
    @FXML
    void ClickReiniciar(MouseEvent event) {
        datos.clear();
        tx_nombre.clear();
        tx_apellido.clear();
        dt_fecha_nac.getEditor().clear();
        try {
            dao.reiniciar();
        } catch (SQLException e) {
            mostrarError(e);
        }
    }

    /** Muestra un diálogo de error con el mensaje de la excepción SQL. */
    private void mostrarError(SQLException e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de base de datos");
        alert.setHeaderText("No se pudo completar la operación");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    /** Muestra un diálogo de aviso con el mensaje indicado. */
    private void mostrarAviso(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
