package app.desktop.javafx.dialog;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

public class Saver {

    enum FILEFORMATS{
        TXT,
        XML,
        JSON,
        HTML
    }
    public static Optional<FILEFORMATS> showSaveDialog(){
        var formats = Arrays.asList(FILEFORMATS.values());

        ChoiceDialog<FILEFORMATS> dialog = new ChoiceDialog<FILEFORMATS>();
        dialog.setTitle("Choose Format");
        dialog.setHeaderText("Choose File format to export.");

        dialog.getItems().addAll(formats);
        dialog.setSelectedItem(FILEFORMATS.TXT);
        return dialog.showAndWait();
    }

    public static void saveResponse(byte[] content,String type)  {
        //Optional<FILEFORMATS> fileformat = showSaveDialog();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileExtensions;
        if (!type.equals("image")) {
            fileExtensions = new FileChooser.ExtensionFilter("TEXT FILES (*.txt), (*.xml), (*.json), (*.html), (*.htm)","*.txt","*xml","*.json","*.html", "*.htm");
        } else {
            fileExtensions = new FileChooser.ExtensionFilter("Image File (*.jpg), (*.jpeg), (*.png)","*.jpg", "*.jpeg", "*.png");
        }
        fileChooser.getExtensionFilters().add(fileExtensions);

        File file = fileChooser.showSaveDialog(null);
        if (file!=null){
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                InputStream inputStream = new ByteArrayInputStream(content);
                inputStream.transferTo(outputStream);
                outputStream.flush();
                outputStream.close();
            }catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
    }
    public static void download(byte[] content) {

    }
}
