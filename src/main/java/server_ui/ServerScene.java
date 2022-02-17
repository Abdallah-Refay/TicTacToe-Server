package server_ui;
import Models.Player;
import controllers.Server;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
public class ServerScene extends Application {
    private boolean started = false ;
    public Button stopServerBtn;
    public Button startServerBtn;
    public Button refreshServerBtn;
    public Label title;
    private TableView<Player> table;
    private TableColumn<Player, String> tableColumn_username;
    private TableColumn<Player, Boolean> tableColumn_online;
    private TableColumn<Player, Integer> tableColumn_losses;
    private TableColumn<Player, Integer> tableColumn_wins;
    private TableColumn<Player, Integer> tableColumn_score;
    private AnchorPane anchor ;
    @Override
    public void start(Stage primaryStage) {
        stopServerBtn = new Button();
        startServerBtn = new Button();
        refreshServerBtn = new Button();
        ///////////////////////////////
        table = new TableView<Player>();
        tableColumn_username = new TableColumn<Player, String>("username");
        tableColumn_username.setId("username");
        tableColumn_username.setPrefWidth(110);
        tableColumn_online = new TableColumn<Player, Boolean>("Online");
        tableColumn_online.setPrefWidth(110);
        tableColumn_losses = new TableColumn<Player, Integer>("losses");
        tableColumn_losses.setPrefWidth(110);
        tableColumn_wins = new TableColumn<Player, Integer>("wins");
        tableColumn_wins.setPrefWidth(110);
        tableColumn_score = new TableColumn<Player, Integer>("score");
        tableColumn_score.setPrefWidth(110);
        if(started == true) {
            table.setItems(Player.getAllUsers());
        }
        tableColumn_username.setCellValueFactory(new PropertyValueFactory<Player, String>("username"));
        tableColumn_online.setCellValueFactory(new PropertyValueFactory<Player, Boolean>("online"));
        tableColumn_losses.setCellValueFactory(new PropertyValueFactory<Player, Integer>("losses"));
        tableColumn_wins.setCellValueFactory(new PropertyValueFactory<Player, Integer>("wins"));
        tableColumn_score.setCellValueFactory(new PropertyValueFactory<Player, Integer>("score"));
        table.getColumns().addAll(tableColumn_username, tableColumn_online, tableColumn_losses, tableColumn_wins,tableColumn_score);
        /////////////////////////////////////////////////////////////
        AnchorPane anchor = new AnchorPane();
        title = new Label();
        anchor.setPrefHeight(400.0);
        anchor.setPrefWidth(600.0);
        anchor.getStyleClass().add("background");
        anchor.getStylesheets().add("/server_ui/Resources/styles.css");
        table.setLayoutX(20.0);
        table.setLayoutY(120.0);
        table.setMinHeight(250);
        table.setMinWidth(558);
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        //layout of start buttons
        startServerBtn.setLayoutX(20.0);
        startServerBtn.setLayoutY(60.0);
        startServerBtn.setMnemonicParsing(false);
        startServerBtn.setPrefHeight(50.0);
        startServerBtn.setPrefWidth(150.0);
        startServerBtn.getStyleClass().add("stop_server_button");
        startServerBtn.getStylesheets().add("/server_ui/Resources/styles.css");
        startServerBtn.setText("Start server");
        //layout of stop buttons
        stopServerBtn.setLayoutX(200.0);
        stopServerBtn.setLayoutY(60.0);
        stopServerBtn.setMnemonicParsing(false);
        stopServerBtn.setPrefHeight(50.0);
        stopServerBtn.setPrefWidth(150.0);
        stopServerBtn.getStyleClass().add("start_server_button");
        stopServerBtn.getStylesheets().add("/server_ui/Resources/styles.css");
        stopServerBtn.setText("Stop server");
        stopServerBtn.setVisible(true);
        //layout of refresh buttons
        refreshServerBtn.setLayoutX(380.0);
        refreshServerBtn.setLayoutY(60.0);
        refreshServerBtn.setMnemonicParsing(false);
        refreshServerBtn.setPrefHeight(50.0);
        refreshServerBtn.setPrefWidth(150.0);
        refreshServerBtn.getStyleClass().add("stop_server_button");
        refreshServerBtn.getStylesheets().add("/server_ui/Resources/styles.css");
        refreshServerBtn.setText("Refresh");
        refreshServerBtn.setVisible(true);
        //////////////////////////////////////////////////
        title.setLayoutX(170.0);
        title.setLayoutY(5.0);
        title.setPrefHeight(58.0);
        title.setPrefWidth(374.0);
        title.getStyleClass().add("logo");
        title.setText("Server Side");
        title.setTextFill(javafx.scene.paint.Color.valueOf("#dbe2e5"));
        title.setFont(new Font("System Bold Italic", 30.0));
        table.setMaxSize(350, 200);
        vbox.getChildren().add(table);
        anchor.getChildren().add(stopServerBtn);
        anchor.getChildren().add(startServerBtn);
        anchor.getChildren().add(refreshServerBtn);
        anchor.getChildren().add(title);
        anchor.getChildren().add(table);
        // new ServerSceneController(this, primaryStage,table,refreshServerBtn);
        // not resizable
        primaryStage.setResizable(false);
        // create scene
        Scene scene = new Scene(anchor);
        scene.getStylesheets().add("server_ui/Resources/styles.css");
        primaryStage.setTitle("server screen");
        primaryStage.setScene(scene);
        primaryStage.show();
        startServerBtn.setOnAction((e)->{
            if (!started) {
                new Server();
                started = true;
                System.out.println("start server");
                table.setItems(Player.getAllUsers());
                table.refresh();
                System.out.println("Refreshed");
                Player.getAllUsers().forEach(player -> System.out.println(player.getId()+" "+player.getUsername()+" "+player.isOnline()));
            }
        });
        stopServerBtn.setOnAction((e)->{
            if(started){
                Server.close();
                started = false;
                table.setItems(null);
                table.refresh();
                System.out.println("close server");
            }
        });
        refreshServerBtn.setOnAction((e)->{
            if (started) {
                table.setItems(Player.getAllUsers());
                table.refresh();
                System.out.println("Refreshed");
                Player.getAllUsers().forEach(player -> System.out.println(player.getId()+" "+player.getUsername()+" "+player.isOnline()));
            }
            else {
                System.out.println("server is closed ");
            }

        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
