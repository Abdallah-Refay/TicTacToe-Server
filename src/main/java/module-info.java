module server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    opens server_ui to javafx.graphics;
    opens server_ui.resources to javafx.graphics;

}