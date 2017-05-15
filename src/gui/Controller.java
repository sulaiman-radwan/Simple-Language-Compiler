package gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import lexer.Lexer;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.InlineCssTextArea;
import parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {

    @FXML
    public InlineCssTextArea console;
    @FXML
    StackPane simpleLanguage;
    @FXML
    StackPane javaLanguage;
    private CodeArea simpleCodeArea = new SimpleKeywords().getCodeArea();
    private CodeArea javaCodeArea = new JavaKeywords().getCodeArea();

    public static Timer timer = new Timer();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        simpleLanguage.getChildren().add(simpleCodeArea);
        simpleLanguage.getStylesheets().add(getClass().getResource("keywords.css").toExternalForm());
        javaLanguage.getChildren().add(javaCodeArea);
        javaLanguage.getStylesheets().add(getClass().getResource("keywords.css").toExternalForm());
        simpleCodeArea.setStyle("-fx-font-family: menlo;-fx-font-size: 14");
        javaCodeArea.setStyle("-fx-font-family: menlo;-fx-font-size: 14");

        simpleCodeArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    compile();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }, 500);
            }
        });

        try {
            compile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void compile() throws FileNotFoundException {
        File input = new File("input.txt");
        File output = new File("output.txt");

        PrintWriter writer = new PrintWriter(input);
        writer.write(simpleCodeArea.getText());
        writer.close();

        try {
            Lexer lexer = new Lexer(input);
            Parser parser = new Parser(lexer, output);
            javaCodeArea.replaceText(parser.program());
            console.replaceText(parser.console.toString());
            console.setStyle("-fx-font-family:consolas");
            for (int i : parser.errors) {
                console.setStyle(i, "-fx-fill:red;-fx-font-size:14;");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}