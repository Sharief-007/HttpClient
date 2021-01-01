package app.desktop.javafx.util;

import app.desktop.javafx.dto.Form;
import app.desktop.javafx.dto.Header;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.net.http.HttpClient.Version.HTTP_1_1;
import static java.net.http.HttpClient.Version.HTTP_2;

public class Processor {

    public enum Methods{
            GET,POST,PUT,DELETE
    }

    public enum RESPONSE_BODY_FORMATS{
        TEXT,
        XML,
        JSON,
        IMAGE
    }

    public static ObservableList<String> getHttpVersions(){
        List<String> httpVersions = new ArrayList<String>();
        HttpClient.Version[] versions = HttpClient.Version.values();
        Arrays.asList(versions).forEach(version -> {
            String v;
            switch(version){
                case HTTP_2 : v = "HTTP 2";
                    break;
                case HTTP_1_1: v = "HTTP 1.1";
                    break;
                default:
                    v = version.toString();
            };
            httpVersions.add(v);
        });
        return FXCollections.observableArrayList(httpVersions);
    }

    public static ObservableList<Methods> getHttpMethods(){
       return FXCollections.observableArrayList(Methods.values());
    }

    public static ObservableList<RESPONSE_BODY_FORMATS> getResponseBodyFormats(){ return FXCollections.observableArrayList(RESPONSE_BODY_FORMATS.values());    }

    public static HttpClient.Version getVersion(String v){
        switch(v){
            case "HTTP 2": return HTTP_2;
            case "HTTP 1.1": return HTTP_1_1;
            default:
                return HTTP_2;
        }
    }

    public static String[] tableToStringArray(TableView<Header> list){
        List<String> headers = new ArrayList<>();
        list.getItems().forEach(h->{
            headers.add(h.getKey());
            headers.add(h.getValue());
        });
        return headers.toArray(new String[headers.size()]);
    }

    public static String prettyPrintXML(String text) throws Exception {
        try{
            Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            //serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            //serializer.setOutputProperty("{http://xml.customer.org/xslt}indent-amount", "2");
            Source xmlSource=new SAXSource(new InputSource(new ByteArrayInputStream(text.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());
            serializer.transform(xmlSource, res);
            return text;
        }catch (Exception e){
            throw new Exception("Error parsing XML ");
        }
    }

    public static String prettyPrintJSON(String text) throws Exception{
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(text);
            return gson.toJson(je);
        }catch (Exception e){
            throw new Exception();
        }
    }

    public static BodyPublisher createBodyPublishersOfFormData(ObservableList<Form> form) {
        var builder = new StringBuilder();
        form.forEach(field ->{
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(field.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(field.getValue().toString(), StandardCharsets.UTF_8));
        });
        System.out.println(builder.toString());
        return BodyPublishers.ofString(builder.toString());
    }

    public static BodyPublisher ofMimeMultipartData(ObservableList<Form> form,
        String boundary) throws Exception {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);
        
        var fields = form.iterator();
        while(fields.hasNext()){
            var field = fields.next();
            byteArrays.add(separator);

            if (field.getValue() instanceof Path) {
                var path = (Path) field.getValue();
                    String mimeType = Files.probeContentType(path);
                    byteArrays.add(("\"" + field.getKey() + "\"; filename=\"" + path.getFileName()
                        + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    byteArrays.add(Files.readAllBytes(path));
                    byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            else {
                byteArrays.add(("\"" + field.getKey() + "\"\r\n\r\n" + field.getValue() + "\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            }
        }

        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        
        return BodyPublishers.ofByteArrays(byteArrays);
    }

    public static boolean isMultiPartFormData(ObservableList<Form> form){
        while (form.iterator().hasNext()) {
            if(form.iterator().next().getValue() instanceof Path){
                return true;
            }
        }
        return false;
    }

    public static void manageHeadersForMultiPart(TableView<Header> table,String boundary) {
        var form = table.getItems();
        boolean alreadyExists = false;

        while (form.iterator().hasNext()) {
            var field = form.iterator().next();
            if (field.getKey().equals("Content-Type")) {
                alreadyExists = true;
                form.remove(field);

                form.add(new Header("Content-Type", "multipart/form-data;boundary=" + boundary));
                break;
            }
        }
        if(!alreadyExists){
            form.add(new Header("Content-Type", "multipart/form-data;boundary=" + boundary));
        }
    }

    public static void manageHeaders(TableView<Header> table){
        var headers = table.getItems();
        var alreadyExists = false;
        while (headers.iterator().hasNext()){
            if (headers.iterator().next().getKey().equals("Content-Type")){
                alreadyExists = true;
            }
        }
        if (!alreadyExists){
            headers.add(new Header("Content-Type","application/x-www-form-urlencoded"));
        }
    }
}
