package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller {

    @FXML
    private MenuBar MenuBar;

    @FXML
    private Menu ConnectionMenu;

    @FXML
    private MenuItem ConnectItem;

    @FXML
    private MenuItem UserName;

    @FXML
    private MenuItem DisconnectItem;

    @FXML
    private Menu EditMenu;

    @FXML
    private MenuItem AddFileItem;

    @FXML
    private ColorPicker ColorChoose;

    @FXML
    private ChoiceBox<String> StyleChoose;

    @FXML
    private TextField TextField;

    @FXML
    private Button Button;

    @FXML
    private TextFlow TextFlow;



    private int port ;
    private Socket socket;
    private String userName;
    private String image;
    private Color color ;
    private String font;
    private ObjectInputStream is;
    private ObjectOutputStream os;

    @FXML
    void initialize() {
        DisconnectItem.setDisable(true);
        color = Color.BLACK;
        initializeUserName();
        initializeChooseItem();
        initializeFontItem();
        initializeFileItem();
        initializeConnectItem();
        initializeDisconnectItem();


    }

    private void initializeFileItem() {
        AddFileItem.setOnAction(e->{
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            fileChooser.getExtensionFilters().addAll(//
                    new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showOpenDialog(new Stage());
            image = file.toURI().toString();

        });
    }

    private void initializeFontItem() {
        StyleChoose.getItems().add("Verdana");
        StyleChoose.getItems().add("Helvetica");
        StyleChoose.setValue("Verdana");
        StyleChoose.setOnAction(e->{
            font = StyleChoose.getValue()  ;
        });
    }

    private void initializeChooseItem() {
        ColorChoose.setOnAction(event -> {
          color = ColorChoose.getValue();
        });
    }

    private void initializeUserName() {
        userName = "User";
        UserName.setOnAction(e->{
            TextInputDialog td = new TextInputDialog("UserF");
            td.setHeaderText("Chooser user name");
            td.showAndWait();
            userName = td.getEditor().getText();
        } );

    }

    private void initializeButton() {

        try {
            os = new ObjectOutputStream(socket.getOutputStream());
            Button.setOnAction(e -> {
                System.out.println(color.toString());
                Packet packet = new Packet(TextField.getText(), userName, image, color.toString(), font);
                try {
                    os.writeObject(packet);
                    os.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                TextField.setText("");

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        private void initializeConnectItem() {
        ConnectItem.setOnAction( e->{
            TextInputDialog td = new TextInputDialog("3440");
            td.setHeaderText("Enter server port");
            td.showAndWait();
            try {
                port = Integer.parseInt(td.getEditor().getText());
                socket = new Socket("localhost", 3440);
                ConnectItem.setDisable(true);
                DisconnectItem.setDisable(false);
                initializeButton();

                Task task = new Task() {
                    @Override
                    protected Void call() throws Exception {
                        startConnection();
                        return null;
                    }
                };
                new Thread(task).start();

            }catch (IOException e1) {
                alertWindow("Can not connect to the server in this port");
            }
        } );

    }

    private void startConnection() throws IOException, ClassNotFoundException {
        Object o;
        is = new ObjectInputStream(socket.getInputStream());
        while(true) {
            while ((o = is.readObject()) != null) {
                Packet packet = (Packet) o;
                System.out.println(packet.toString());
                addToTextFlow(packet);
            }
        }
    }

    private void addToTextFlow(Packet packet) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Text text = new Text(packet.getUserName() + " : " + packet.getMessage()+"\n");
                text.setFill(Color.web(packet.getFontColor()));
                text.setFont(Font.font(packet.getFontStyle(), FontWeight.EXTRA_BOLD , 15));
                TextFlow.getChildren().add(text);
                if(packet.getImage() != null && !packet.getImage().equalsIgnoreCase("")) {
                    ImageView imageView = new ImageView(new Image(packet.getImage()));
                    imageView.setFitHeight(320);
                    imageView.setFitWidth(280);
                    TextFlow.getChildren().add(imageView);
                    TextFlow.getChildren().add(new Text("\n"));
                    image = "";
                }
            }
        });
    }

    private void initializeDisconnectItem(){
        DisconnectItem.setOnAction( e->{
            try {
              socket.close();
              TextFlow.getChildren().clear();
              ConnectItem.setDisable(false);
              DisconnectItem.setDisable(true);
            }catch (IOException e1) {
                alertWindow("Can not disconnect from the server");
            }
        } );
    }

    private void alertWindow(String contentText){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}

