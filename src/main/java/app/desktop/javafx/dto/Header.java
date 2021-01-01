package app.desktop.javafx.dto;

import javafx.beans.property.SimpleStringProperty;

public class Header {
    private SimpleStringProperty key,value;

    public Header(String key, String value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public String getKey() {
        return key.get();
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    @Override
    public String toString() {
        return "Header{" +
                "key=" + key.get() +
                ", value=" + value.get() +
                '}';
    }
}
