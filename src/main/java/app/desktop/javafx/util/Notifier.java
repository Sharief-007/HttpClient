package app.desktop.javafx.util;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class Notifier {

    public static Optional<Pair<String, String>> showKeyValueDialog(String title){

        //create Content
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefSize(300.0,200.0);
        vBox.setSpacing(15.0);

        //key
        Label keyLabel = new Label("key");
        keyLabel.setPadding(new Insets(0,5.0,0,5.0));
        TextField key = new TextField();
        key.setPromptText("Enter header key");
        HBox keybox = new HBox(keyLabel,key);
        keybox.setSpacing(15.0);
        keybox.setAlignment(Pos.CENTER);

        //value
        Label valueLabel = new Label("value");
        TextField value = new TextField();
        value.setPromptText("Enter header value");
        HBox valuebox = new HBox(valueLabel,value);
        valuebox.setSpacing(15.0);
        valuebox.setAlignment(Pos.CENTER);

        vBox.getChildren().addAll(keybox,valuebox);

        //set Dailog
        Dialog<Pair<String,String>> dailog = new Dialog<>();
        dailog.setTitle("New "+title);
        dailog.setResizable(false);
        dailog.getDialogPane().setContent(vBox);


        //add Buttons
        ButtonType ok = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        dailog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ok);


        //apply button
        Node apply = dailog.getDialogPane().lookupButton(ok);

        //set disabled initially
        apply.setDisable(true);


        key.textProperty().addListener(e->{
            apply.setDisable(key.getText().isEmpty()||value.getText().isEmpty());
        });
        value.textProperty().addListener(e->{
            apply.setDisable(value.getText().isEmpty()||value.getText().isEmpty());
        });

        //result
        dailog.setResultConverter( buttonType-> {
            if (buttonType.equals(ok) ) {
                return new Pair<>(key.getText(), value.getText());
            }
            return null;
        });

        Platform.runLater(key::requestFocus);
        Optional<Pair<String, String>> result = dailog.showAndWait();
        return result;
    }
    public static Optional<Pair<String, String>> showKeyValueDialogWithFile(){

        //create Content
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefSize(300.0,200.0);
        vBox.setSpacing(15.0);

        //key
        Label keyLabel = new Label("key");
        keyLabel.setPadding(new Insets(0,5.0,0,5.0));
        TextField key = new TextField();
        key.setPromptText("Enter header key");
        HBox keybox = new HBox(keyLabel,key);
        keybox.setSpacing(15.0);
        keybox.setAlignment(Pos.CENTER);

        //value
        Label valueLabel = new Label("value");
        Button value = new Button("Choose File");
        HBox valuebox = new HBox(valueLabel,value);
        valuebox.setSpacing(15.0);
        value.setPrefWidth(150.0);
        valuebox.setAlignment(Pos.CENTER);


        AtomicReference<File> file = new AtomicReference<>();
        SimpleBooleanProperty disable = new SimpleBooleanProperty(true);
        value.setOnAction(e->{
            var filechooser = new FileChooser();
            file.set(filechooser.showOpenDialog(null));
            if (file!=null && !key.getText().isEmpty()){
                disable.set(false);
            }
        });


        vBox.getChildren().addAll(keybox,valuebox);

        //set Dailog
        Dialog<Pair<String,String>> dailog = new Dialog<>();
        dailog.setTitle("New Form Field");
        dailog.setResizable(false);
        dailog.getDialogPane().setContent(vBox);


        //add Buttons
        ButtonType ok = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        dailog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ok);


        //apply button
        Node apply = dailog.getDialogPane().lookupButton(ok);

        //set disabled initially
        apply.setDisable(true);


        key.textProperty().addListener(e->{
            disable.set(key.getText().isEmpty()||file.get()==null);
        });
        apply.disableProperty().bindBidirectional(disable);


        //result
        dailog.setResultConverter( buttonType-> {
            if (buttonType.equals(ok) ) {
                return new Pair<>(key.getText(), file.get().getAbsolutePath());
            }
            return null;
        });

        Platform.runLater(key::requestFocus);

        return dailog.showAndWait();
    }
}

