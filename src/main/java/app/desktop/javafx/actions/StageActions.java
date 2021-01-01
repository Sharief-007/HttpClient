package app.desktop.javafx.actions;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageActions {


    private final Resource logo;
    private final String title;
    private final Resource ApplicationFxml;
    private final ApplicationContext context;

    public StageActions(@Value("${app.stage.icon}")Resource logo,
                        @Value("${spring.application.name}") String title,
                        @Value("${app.fxml.first-file}") Resource applicationFxml,
                        ApplicationContext context) {
        this.logo = logo;
        this.title = title;
        ApplicationFxml = applicationFxml;
        this.context = context;
    }

    public void setCloseRequestHandler(Stage stage){
        stage.setOnCloseRequest(this::handleCloseRequest);
    }

    private void handleCloseRequest(WindowEvent windowEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure you want to exit the Application ?");
        alert.setContentText("Saving or Downloading the data is recommended else all the data may be lost on closing the application.");
        var button = alert.showAndWait();
        button.ifPresentOrElse(buttonType -> {
            if (buttonType.getButtonData().isCancelButton()){
                windowEvent.consume();
            }
        }, windowEvent::consume);
    }

    public void setTitleAndIcon(Stage stage){
        try {
            stage.setTitle(title);
            stage.getIcons().add(new Image(logo.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMainFxml(Stage stage){
        try {
            var loader = new FXMLLoader(ApplicationFxml.getURL());
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            //log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
