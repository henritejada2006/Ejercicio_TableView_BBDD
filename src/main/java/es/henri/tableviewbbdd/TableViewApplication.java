package es.henri.tableviewbbdd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TableViewApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TableViewApplication.class.getResource("Tableview-View.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 435, 500);
        stage.setTitle("Añadir/Eliminar columnas de un TableView");
        stage.setScene(scene);
        stage.setMinWidth(435);
        stage.setMinHeight(500);
        stage.show();
    }
}
