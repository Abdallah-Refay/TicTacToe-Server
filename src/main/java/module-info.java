module com.example.tictacserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tictacserver to javafx.fxml;
    exports com.example.tictacserver;
}