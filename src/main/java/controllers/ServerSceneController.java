package controllers;

import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import server_ui.ServerScene;

public class ServerSceneController {

    private ServerScene serverScene;
    private boolean started = false;
    private Button  refreshbtnn;

    public ServerSceneController(ServerScene serverScene1, Stage primaryStage, TableView table, Button refreshServerBtn) {
        //refreshbtnn = refreshServerBtn;
        //serverScene = serverScene1;
        //serverScene1.startServerBtnHandler(serverStart(primaryStage));
        //serverScene1.stopServerBtnHandler(serverStop(primaryStage));
        //serverScene1.refreshServerBtnHandler(serverRefresh(primaryStage,serverScene1,table));
        //PauseTransition pause = new PauseTransition(Duration.seconds(1));
        //pause.setOnFinished(event -> {
       // });
        //pause.playFromStart();
}
/*
    private EventHandler<ActionEvent> serverStart(Stage primaryStage) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!started) {
                    new Server();
                    started = true;
                }
            }
        };
    }
    private EventHandler<ActionEvent> serverStop(Stage primaryStage) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (started) {
                    Server.close();
                    started = false;

                    System.out.println("close server");
                }
            }
        };
    }
    private EventHandler<ActionEvent> serverRefresh(Stage primaryStage,ServerScene serverScene1,TableView table) {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                 table.setItems(serverScene1.getAllUsers());
                 table.refresh();
                 System.out.println("Refreshed");
            }
        };
    }
    public Button refreshBtn(){
           return  refreshbtnn;
    }*/
}


