module autopartes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j; 

    opens com.autopartes to javafx.fxml;
    opens com.autopartes.Controlador to javafx.fxml;
    opens com.autopartes.Modelo to javafx.base, javafx.fxml;
    
    exports com.autopartes;
}
