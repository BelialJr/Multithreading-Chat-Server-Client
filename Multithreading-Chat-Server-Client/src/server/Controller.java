package server;

import client.Packet;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    @FXML
    private Tab ServerlaunchTab;

    @FXML
    private SplitPane SplitPane;

    @FXML
    private ListView<User> UsersList;

    @FXML
    private Label HostLabel;

    @FXML
    private Label PortLabel;

    @FXML
    private Label UsersLabel;

    @FXML
    private TextField HostTextField;

    @FXML
    private TextField PortTextField;

    @FXML
    private TextField UsersTextField;

    @FXML
    public Button StartButton;

    @FXML
    private Label StatusLabel;

    @FXML
    private Label StatusText;

    @FXML
    private Label ConnectedLabel;

    @FXML
    private Label ConnectedText;

    @FXML
    private Button StopButton;

    @FXML
    private Tab ServerlLogsTab;


    @FXML
    private  ListView<String> ServerLogsListView;


    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    private LocalDateTime now = LocalDateTime.now();
    private boolean serverIsStarted = false;
    private  Integer port = null;
    private  ServerSocket server ;
    private static int  _token = 1;
    private static int currentToken ;
    private  Connection con;
    private ArrayList<Connection> connectionList = new ArrayList<>();
    @FXML
    void initialize() throws IOException {
        HostTextField.setEditable(false);
        StatusText.setTextFill(Color.RED);
        initializeSplitPane();
        initializeStartButton();
        initializeStopButton();
        ConnectedCountListener();
    }

    private void initializeSplitPane() {

    }

    private void startServer(Integer port,int maxUsers) {
        if (serverIsStarted) {
            PortTextField.setDisable(true);
            UsersTextField.setDisable(true);
            try {
                server = new ServerSocket(port, maxUsers);
                addToLogs("Server is starting on port " + port + ", with max " + maxUsers + " users");
                StatusText.setText("online");
                StatusText.setTextFill(Color.GREEN);

            } catch (IOException e) {
                alertWindow("Failed to start server on port " + port, "Failed to start server on port " + port);
            }
        }
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    while (serverIsStarted) {
                        Socket socket = server.accept();
                        currentToken = _token++;
                        User user = new User(socket.getInetAddress().getHostName(), socket.getPort(), currentToken);
                        UsersList.getItems().add(user);
                        //updateConnectedCount();
                        addToLogs("User [ ip: " + socket.getInetAddress().getHostName() + " : port: " + socket.getPort() + " ; Token: " + currentToken + "] joined the server");
                        new Thread( () ->{
                            try {
                                con = new Connection(socket,this::sendFunction);
                                connectionList.add(con);
                                con.startReading();

                            } catch (IOException | ClassNotFoundException e) {
                               addToLogs(user +   " : just left  the chat");
                               removeFromUserList(user);
                             //  updateConnectedCount();
                            }


                        }).start();
                    }
                } catch (IOException ignore) {
                  server.close();
                }
                return null;
            }

            private void sendFunction(Packet packet) {
                addToLogs("User message : "+packet.getUserName() + " : "+ packet.getMessage());

                for(Connection connection : connectionList){
                    try {
                        connection.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        };
        new Thread(task).start();
    }



    private void ConnectedCountListener() {
        AnimationTimer animationTimer=new AnimationTimer() {
            @Override
            public void handle(long now) {
                ConnectedText.setText(String.valueOf(UsersList.getItems().size()));
            }
        };
        animationTimer.start();
    }
    private void removeFromUserList(User user){
        AnimationTimer animationTimer=new AnimationTimer() {
            @Override
            public void handle(long now) {
                UsersList.getItems().remove(user);
            }
        };
        animationTimer.start();
    }
    private void addToUserList(User user){
        AnimationTimer animationTimer=new AnimationTimer() {
            @Override
            public void handle(long now) {
                UsersList.getItems().add(user);
                return;
            }
        };
        animationTimer.start();
    }


    private void stopServer(){
        try {
            server.close();
            StatusText.setText("offline");
            StatusText.setTextFill(Color.RED);
            PortTextField.setDisable(false);
            UsersTextField.setDisable(false);
            UsersList.getItems().clear();
            addToLogs("Server has been stopped");
        } catch (IOException e) {
            alertWindow("Failed to stop server "  ,"Failed to stop server" );
        }
    }

    private Integer getPort(){
        Integer result = null;
        try {
            result = Integer.parseInt(PortTextField.getText());
        }catch (NumberFormatException e ){
            alertWindow("Can not start server in this port","Failed to start a server in port " + PortTextField.getText());
            serverIsStarted = false;

            return null;
        }
        if(result > 65535){PortTextField.setText("65535");return 65535; }
        return  result;
    }

    private int getMaxUsersCount(){
        int res = 0;
        try {
            res = Integer.parseInt(UsersTextField.getText());
        }catch (NumberFormatException e ){
            alertWindow("Can not start server with max count "+ UsersTextField.getText(),"Can not start server with max count "+ UsersTextField.getText());
            serverIsStarted = false;

            return 0;
        }
        if(res < 0 ){UsersTextField.setText("1");return 1;}
        if(res > 20){UsersTextField.setText("20");return 20;}
        return res;
    }

    private void initializeStartButton() {
        StartButton.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent event) {
                if(!serverIsStarted)
                {
                    serverIsStarted = true;
                    port = getPort();
                    int maxUsers = getMaxUsersCount();
                    startServer(port,maxUsers);
                }else{
                    alertWindow("Server is already started","Failed to start a server (Already started)");
                }
            }
        });
    }

    private void initializeStopButton() {
        StopButton.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent event) {
                if(serverIsStarted)
                {
                    serverIsStarted = false;
                    stopServer();
                }
            }
        });
    }
    private void addToLogs(String str){

        Platform.runLater(new Runnable() {
            @Override public void run() {
                ServerLogsListView.getItems().add(dtf.format(LocalDateTime.now()) + " : [Server] : " + str);
            }
        });

    }
    private void alertWindow(String contentText, String logs){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        addToLogs(logs);
        alert.showAndWait();
    }
}


