module server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    opens com.example.server to javafx.fxml;
    exports com.example.server;
}