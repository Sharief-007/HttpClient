package app.desktop.javafx.actions;

import app.desktop.javafx.dto.Form;
import app.desktop.javafx.dto.Header;
import app.desktop.javafx.util.Notifier;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class TableActions {
    public void addFileFieldToTable(TableView<Form> table){
        var result = Notifier.showKeyValueDialogWithFile();
        result.ifPresent(p->{
            var file = Path.of(p.getValue());
            var formfield = new Form(p.getKey(),file);
            table.getItems().add(formfield);
        });
    }

    public void addHeaderToTable(TableView<Header> table){
        var result = Notifier.showKeyValueDialog("Header");
        result.ifPresent(p->{
            var header = new Header(p.getKey(),p.getValue());
            table.getItems().add(header);
        });
    }

    public void addTextFieldToTable(TableView<Form> table){
        var result = Notifier.showKeyValueDialog("Form Field");
        result.ifPresent(p->{
            var formfield = new Form(p.getKey(),p.getValue());
            table.getItems().add(formfield);
        });
    }

    public void removeFieldFromTable(TableView<Form> table){
        var formfield = table.getSelectionModel().getSelectedItem();
        if (formfield!=null){
            table.getItems().remove(formfield);
        }
    }

    public void removeHeaderFromTable(TableView<Header> table){
        var header = table.getSelectionModel().getSelectedItem();
        if (header!=null){
            table.getItems().remove(header);
        }
    }
}
