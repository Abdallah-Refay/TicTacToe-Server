module server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.kordamp.bootstrapfx.core;
    opens com.example.server to javafx.fxml;
    exports com.example.server;
}