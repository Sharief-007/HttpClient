package app.desktop.javafx.controller;

import app.desktop.javafx.actions.MainPageInitializer;
import app.desktop.javafx.actions.TableActions;
import app.desktop.javafx.dialog.ErrorDialog;
import app.desktop.javafx.dialog.Saver;
import app.desktop.javafx.dto.Form;
import app.desktop.javafx.dto.Header;
import app.desktop.javafx.util.API;
import app.desktop.javafx.util.Processor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MainController {

    @FXML
    private TextField URL_TEXT_FIELD;

    @FXML
    private ChoiceBox<String> HTTP_VERSION;

    @FXML
    private ChoiceBox<Processor.Methods> REQUEST_METHOD;

    @FXML
    private Button SEND;

    @FXML
    private TableView<Header> REQUEST_HEADERS;

    @FXML
    private TableColumn<Header, String> REQUEST_HEADERS_KEY_COLUMN;

    @FXML
    private TableColumn<Header, String> REQUEST_HEADERS_VALUE_COLUMN;

    @FXML
    private TabPane REQUEST_BODY_TABS;

    @FXML
    private Tab RAWDATA_TAB;

    @FXML
    private TextArea RAWDATA_TEXT_AREA;

    @FXML
    private Tab FORMDATA_TAB;

    @FXML
    private TableView<Form> FORM_DATA_TABLE;

    @FXML
    private TableColumn<Form, String> FORM_DATA_KEY_COLUMN;

    @FXML
    private TableColumn<Form, Object> FORM_DATA_VALUE_COLUMN;

    @FXML
    private Tab BINARYDATA_TAB;

    @FXML
    private Button FILECHOOSER;

    @FXML
    private Label FILENAME;

    @FXML
    private RadioButton NONE;

    @FXML
    private RadioButton RAW_DATA;

    @FXML
    private RadioButton FORM_DATA;

    @FXML
    private RadioButton BINARYDATA;

    @FXML
    private Label STATUS;

    @FXML
    private Button COPY;

    @FXML
    private Button EXPORT;

    @FXML
    private TabPane RESPONSE_TABS;

    @FXML
    private TableView<Header> RESPONSE_HEADERS;

    @FXML
    private TableColumn<Header, String> RESPONSE_HEADERS_KEY_COLUMN;

    @FXML
    private TableColumn<Header, String> RESPONSE_HEADERS_VALUE_COLUMN;

    @FXML
    private ChoiceBox<Processor.RESPONSE_BODY_FORMATS> RESPONSE_BODY_PARSER;

    @FXML
    private TextArea RESPONSE_BODY;

    @FXML
    private WebView WEB_VIEW;

    @FXML
    private Tab PREVIEW_IMAGE_TAB;

    @FXML
    private Button DOWNLOAD;

    @FXML
    private ImageView BLOBIMAGE;


    private File fileToUpload;
    private final TableActions tableActions;
    private final MainPageInitializer mainPageInitializer;

    public MainController(TableActions tableActions, MainPageInitializer mainPageInitializer) {
        this.tableActions = tableActions;
        this.mainPageInitializer = mainPageInitializer;
    }

    @FXML
    void addFileFieldToTable() {
        tableActions.addFileFieldToTable(FORM_DATA_TABLE);
    }

    @FXML
    void addHeaderToTable() {
        tableActions.addHeaderToTable(REQUEST_HEADERS);
    }

    @FXML
    void addTextFieldToTable() {
        tableActions.addTextFieldToTable(FORM_DATA_TABLE);
    }

    @FXML
    void removeFieldFromTable() {
        tableActions.removeFieldFromTable(FORM_DATA_TABLE);
    }

    @FXML
    void removeHeaderFromTable() {
        tableActions.removeHeaderFromTable(REQUEST_HEADERS);
    }

    @FXML
    void sendRequest() {

        if (!URL_TEXT_FIELD.getText().isEmpty()){
            try {
                //set process started
                SEND.setDisable(true);
                STATUS.setText("Processing Request....");
                //RESPONSE_HEADERS.setItems(null);


                var url = URI.create(URL_TEXT_FIELD.getText());
                var HttpVersion = Processor.getVersion(HTTP_VERSION.getValue());
                var method = REQUEST_METHOD.getValue();

                HttpRequest.BodyPublisher bodyPublisher;
                if (NONE.isSelected()){
                    bodyPublisher = HttpRequest.BodyPublishers.noBody();
                }else if (RAW_DATA.isSelected() && !RAWDATA_TEXT_AREA.getText().isEmpty()){
                    bodyPublisher = HttpRequest.BodyPublishers.ofString(RAWDATA_TEXT_AREA.getText());
                }else if (FORM_DATA.isSelected() && FORM_DATA_TABLE.getItems().size()!=0){
                    var form = FORM_DATA_TABLE.getItems();
                    if (Processor.isMultiPartFormData(form)){
                        String boundary = new BigInteger(256, new Random()).toString();
                        bodyPublisher = Processor.ofMimeMultipartData(form, boundary);
                        Processor.manageHeadersForMultiPart(REQUEST_HEADERS, boundary);
                    }else{
                        bodyPublisher = Processor.createBodyPublishersOfFormData(form);
                        Processor.manageHeaders(REQUEST_HEADERS);
                    }
                }else if (BINARYDATA.isSelected() && fileToUpload!=null){
                    bodyPublisher = HttpRequest.BodyPublishers.ofFile(Path.of(fileToUpload.getAbsolutePath()));
                }else {
                    bodyPublisher = HttpRequest.BodyPublishers.noBody();
                }



                var headers = Processor.tableToStringArray(REQUEST_HEADERS);
                Platform.runLater(()->{
                    var api = new API(url,HttpVersion,method, headers,bodyPublisher);
                    try {
                        var response = api.sendRequest();

                        handleResponse(response);
                    } catch (Exception e) {
                        ErrorDialog.showDetailedError("Error Sending Request.",e);
                        SEND.setDisable(false);
                        STATUS.setText("");
                    }
                });
            }catch (Exception e){
                ErrorDialog.showDetailedError("Error Occured",e);
                //reset the states
                SEND.setDisable(false);
                STATUS.setText("");
            }
        }
    }

    private String RESPONSE = null;
    private HttpResponse<byte[]> HTTPRESPONSE = null;
    private void handleResponse(CompletableFuture<HttpResponse<byte[]>> future) throws Exception {
        var headers = new ArrayList<Header>();
        AtomicInteger statusCode = new AtomicInteger();

        //set defaults on response side
        RESPONSE_BODY_PARSER.setValue(Processor.RESPONSE_BODY_FORMATS.TEXT);
        PREVIEW_IMAGE_TAB.setDisable(true);

        try {
            long start = System.currentTimeMillis();
            future.thenApplyAsync(httpResponse -> {
                httpResponse.headers().map().forEach((k,v)-> headers.add(new Header(k,v.toString())));
                Platform.runLater(()->RESPONSE_HEADERS.setItems(FXCollections.observableArrayList(headers)));
                return httpResponse;
            }).thenApplyAsync(httpResponse->{
                RESPONSE = new String(httpResponse.body());
                Platform.runLater(()->printResponse(RESPONSE));
                return httpResponse;
            }).thenAcceptAsync(httpResponse -> {
                statusCode.set(httpResponse.statusCode());
                HTTPRESPONSE = httpResponse;
            }).thenRunAsync(()->{
                SEND.setDisable(false);
                Platform.runLater(()->{
                    STATUS.setText("Response Status : "+statusCode.get()+"\tTime : "+(System.currentTimeMillis()-start)+" ms");
                    WEB_VIEW.getEngine().loadContent(RESPONSE);
                });
            });
        }catch (Exception e) {throw new Exception();}
    }


    @FXML
    public void initialize() {
        //intitalize components for request
        mainPageInitializer.initTextField(URL_TEXT_FIELD);
        mainPageInitializer.setHttpVersions(HTTP_VERSION);
        mainPageInitializer.setHttpMethods(REQUEST_METHOD);

        mainPageInitializer.setRequestHeaders(REQUEST_HEADERS_KEY_COLUMN,REQUEST_HEADERS_VALUE_COLUMN,REQUEST_HEADERS);
        mainPageInitializer.setFormDataTable(FORM_DATA_KEY_COLUMN,FORM_DATA_VALUE_COLUMN,FORM_DATA_TABLE);
        setToggleGroupForRequestBodyType();
        initFileChooser();


        //intitalize components for response
        setResponseTypes();
        mainPageInitializer.setResponseHeaders(RESPONSE_HEADERS_KEY_COLUMN,RESPONSE_HEADERS_VALUE_COLUMN,RESPONSE_HEADERS);
        initCopyButton();
        initExportButton();

        //setDefaults()
        setDefaults();
    }

    ToggleGroup toggleGroup = new ToggleGroup();

    private void setToggleGroupForRequestBodyType() {
        NONE.setToggleGroup(toggleGroup);
        RAW_DATA.setToggleGroup(toggleGroup);
        FORM_DATA.setToggleGroup(toggleGroup);
        BINARYDATA.setToggleGroup(toggleGroup);

        toggleGroup.selectedToggleProperty().addListener((a,b,it)->{
            if (it.equals(NONE)){
                REQUEST_BODY_TABS.setDisable(true);
            }else if (it.equals((RAW_DATA))){
                //enable tabpane
                REQUEST_BODY_TABS.setDisable(false);
                //enable and select tab
                RAWDATA_TAB.setDisable(false);
                REQUEST_BODY_TABS.getSelectionModel().select(RAWDATA_TAB);
                //disable others
                FORMDATA_TAB.setDisable(true);
                BINARYDATA_TAB.setDisable(true);
            }else if (it.equals(FORM_DATA)){
                //enable tabpane
                REQUEST_BODY_TABS.setDisable(false);
                //enable and select tab
                FORMDATA_TAB.setDisable(false);
                REQUEST_BODY_TABS.getSelectionModel().select(FORMDATA_TAB);
                //disable others
                RAWDATA_TAB.setDisable(true);
                BINARYDATA_TAB.setDisable(true);
            }else if (it.equals(BINARYDATA)){
                //enable tabpane
                REQUEST_BODY_TABS.setDisable(false);
                //enable and select tab
                BINARYDATA_TAB.setDisable(false);
                REQUEST_BODY_TABS.getSelectionModel().select(BINARYDATA_TAB);
                //disable others
                FORMDATA_TAB.setDisable(true);
                RAWDATA_TAB.setDisable(true);
            }
        });
    }


    private void setResponseTypes() {
        RESPONSE_BODY_PARSER.setItems(Processor.getResponseBodyFormats());
        RESPONSE_BODY_PARSER.valueProperty().addListener((observableValue, response_body_format, t1) -> {
            if (t1== Processor.RESPONSE_BODY_FORMATS.TEXT && RESPONSE!=(null) && !RESPONSE.isEmpty()){
                printResponse(RESPONSE);
            } else if (t1== Processor.RESPONSE_BODY_FORMATS.XML && RESPONSE!=(null) && !RESPONSE.isEmpty()){
                //parse response to xml
                try {
                    var xml = Processor.prettyPrintXML(RESPONSE);
                    RESPONSE_BODY.setText(xml);
                }catch (Exception exception){
                    ErrorDialog.showError("Looks like the response text is not in XML format. \n",exception);
                    //don't change values
                    RESPONSE_BODY_PARSER.setValue(response_body_format);
                }
            }else if (t1== Processor.RESPONSE_BODY_FORMATS.JSON && RESPONSE!=(null) && !RESPONSE.isEmpty()){
                try {
                    var json = Processor.prettyPrintJSON(RESPONSE);
                    RESPONSE_BODY.setText(json);
                }catch (Exception exception){
                    ErrorDialog.showError("Looks like the response text is not in JSON format. \n",exception);
                    //don't change values
                    RESPONSE_BODY_PARSER.setValue(response_body_format);
                }
            }else if (t1== Processor.RESPONSE_BODY_FORMATS.IMAGE && HTTPRESPONSE!=null){
                try {
                    InputStream stream = new ByteArrayInputStream(HTTPRESPONSE.body());
                    Image image = new Image(stream);
                    if (image.isError()){
                        throw new Exception();
                    }
                    else {
                        PREVIEW_IMAGE_TAB.setDisable(false);
                        BLOBIMAGE.setImage(image);
                        BLOBIMAGE.setFitHeight(image.getHeight());
                        BLOBIMAGE.setFitWidth(image.getWidth());
                        RESPONSE_TABS.getSelectionModel().selectLast();
                    }
                }catch (Exception exception){
                    ErrorDialog.showDetailedError("Looks like the response is not in IMAGE format. \n",exception);
                    //don't change values
                    RESPONSE_BODY_PARSER.setValue(response_body_format);
                }
            }
        });
    }


    private void initCopyButton(){
        RESPONSE_BODY.textProperty().addListener(event->{
            COPY.setDisable(RESPONSE_BODY.getText().isEmpty());
            EXPORT.setDisable(RESPONSE_BODY.getText().isEmpty());
        });
        COPY.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(RESPONSE_BODY.getText());
            Platform.runLater(()->Clipboard.getSystemClipboard().setContent(content));
        });
    }

    private void initExportButton(){
        EXPORT.setOnAction(event -> Saver.saveResponse(HTTPRESPONSE.body(),"Text"));
        DOWNLOAD.setOnAction(event -> Saver.saveResponse(HTTPRESPONSE.body(),"image"));
    }

    private void setDefaults(){

        HTTP_VERSION.setValue(Processor.getHttpVersions().get(1));
        REQUEST_METHOD.setValue(Processor.Methods.GET);
        NONE.setSelected(true);

        //set default body parser
        RESPONSE_BODY_PARSER.setValue(Processor.RESPONSE_BODY_FORMATS.TEXT);
        PREVIEW_IMAGE_TAB.setDisable(true);
        RESPONSE_BODY.setText("");


    }

    private void initFileChooser() {
        FILECHOOSER.setOnAction(event->{
            fileToUpload = new FileChooser().showOpenDialog(FILECHOOSER.getScene().getWindow());
            if (fileToUpload!=null){
                FILENAME.setText("Selected File :"+fileToUpload.getName());
            }
        });
    }
    private void printResponse(String response) {
        RESPONSE_BODY.setText(response);
    }

}
