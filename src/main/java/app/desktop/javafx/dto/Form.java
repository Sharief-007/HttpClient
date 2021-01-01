package app.desktop.javafx.dto;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Form {
    SimpleStringProperty key;
    SimpleObjectProperty value;

    public Form(String key, Object value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleObjectProperty(value);
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

    public Object getValue() {
        return value.get();
    }

    public SimpleObjectProperty valueProperty() {
        return value;
    }

    public void setValue(Object value) {
        this.value.set(value);
    }
}
