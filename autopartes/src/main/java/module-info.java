module autopartes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    // Intentamos con el nombre de módulo automático estándar
    requires mysql.connector.j; 

    // Abrimos los paquetes para que Scene Builder y JavaFX funcionen
    opens com.autopartes to javafx.fxml;
    opens com.autopartes.Controlador to javafx.fxml;
    
    exports com.autopartes;
}
