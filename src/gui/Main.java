package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.getIcons().add(new Image("icon.png"));
        primaryStage.setTitle("Simple Language Compiler");
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.show();
    }
    @Override
    public void stop(){
        Controller.timer.cancel();
    }
    public static void main(String[] args) {
        launch(args);
    }
}


