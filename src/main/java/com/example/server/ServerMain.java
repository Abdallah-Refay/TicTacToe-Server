package com.example.server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

import java.sql.DriverManager;
import java.sql.SQLException;

public class ServerMain extends Application {
    public static ServerDatabases serverBuild;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerMain.class.getResource("server.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("DBMS!");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) throws SQLException {
        launch();

    }
}
