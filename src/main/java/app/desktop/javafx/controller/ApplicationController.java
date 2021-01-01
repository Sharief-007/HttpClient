package app.desktop.javafx.controller;

import app.desktop.javafx.actions.TabActions;
import app.desktop.javafx.dialog.ErrorDialog;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApplicationController {
    @FXML
    private TabPane APPLICATION_TABS;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TabActions tabActions;

    public ApplicationController(TabActions tabActions) {
        this.tabActions = tabActions;
    }

    @FXML
    public void initialize() {
        try {
            var tab = tabActions.createNewTab();
            APPLICATION_TABS.getTabs().add(tab);
        } catch (IOException e) {
            log.error(e.getMessage());
            ErrorDialog.showDetailedError("Error Occurred while creating initial tab",e);
        }
    }

    @FXML
    void createAndOpenHttpClient() {
        try{
            var tab = tabActions.createNewTab();
            APPLICATION_TABS.getTabs().add(tab);
            APPLICATION_TABS.getSelectionModel().select(tab);
        } catch (IOException e) {
            log.error(e.getMessage());
            ErrorDialog.showError("Can't Open a new Tab",e);
        }

    }

    @FXML
    void createAndOpenWebSocketClient() {
        //do nothing for now
    }

    @FXML
    void closeTab(){
        if (APPLICATION_TABS.getTabs().size()!=0){
            int index = APPLICATION_TABS.getSelectionModel().getSelectedIndex();
            APPLICATION_TABS.getTabs().remove(index);
        }
    }

    @FXML
    void closeAllTabs(){
        APPLICATION_TABS.getTabs().removeAll(APPLICATION_TABS.getTabs());
    }

    @FXML
    void quit() {
        var window = this.APPLICATION_TABS.getScene().getWindow();
        window.fireEvent(new WindowEvent(window,WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
