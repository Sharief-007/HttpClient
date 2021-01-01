package app.desktop.javafx.actions;

import app.desktop.javafx.dto.Form;
import app.desktop.javafx.dto.Header;
import app.desktop.javafx.util.Processor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MainPageInitializer {

    public void initTextField(TextField input) {
        Platform.runLater(input::requestFocus);
        input.focusedProperty().addListener(event-> Platform.runLater(()-> {
            if (Objects.nonNull(input) && input.isFocused() && !input.getText().isEmpty()) {
                input.selectAll();
            }
        }));
    }

    public void setHttpMethods(ChoiceBox<Processor.Methods> httpMethods) {
        httpMethods.setItems(Processor.getHttpMethods());
    }

    public void setHttpVersions(ChoiceBox<String> httpVersion) {
        httpVersion.setItems(Processor.getHttpVersions());
    }

    public void setRequestHeaders(TableColumn<Header, String> key_column,
                                  TableColumn<Header, String> value_column,
                                  TableView<Header> request_headers) {
        ObservableList<Header> headers= FXCollections.observableArrayList();
        key_column.setCellValueFactory(new PropertyValueFactory<>("key"));
        value_column.setCellValueFactory(new PropertyValueFactory<>("value"));
        request_headers.setItems(headers);
    }

    public void setFormDataTable(TableColumn<Form, String> key_column, TableColumn<Form, Object> value_column, TableView<Form> form_data_table) {
        ObservableList<Form> form = FXCollections.observableArrayList();
        key_column.setCellValueFactory(new PropertyValueFactory<>("key"));
        value_column.setCellValueFactory(new PropertyValueFactory<>("value"));
        form_data_table.setItems(form);
    }

    public void setResponseHeaders(TableColumn<Header, String> key_column, TableColumn<Header, String> value_column, TableView<Header> response_headers) {
        ObservableList<Header> headers= FXCollections.observableArrayList();
        key_column.setCellValueFactory(new PropertyValueFactory<>("key"));
        value_column.setCellValueFactory(new PropertyValueFactory<>("value"));
        response_headers.setItems(headers);
    }
}
