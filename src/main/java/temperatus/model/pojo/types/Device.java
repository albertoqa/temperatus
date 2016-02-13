package temperatus.model.pojo.types;

import com.dalsemi.onewire.container.OneWireContainer;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by alberto on 13/2/16.
 */
public class Device {

    private SimpleStringProperty defaultPosition = new SimpleStringProperty();
    private SimpleStringProperty serial = new SimpleStringProperty();
    private SimpleStringProperty model = new SimpleStringProperty();
    private SimpleStringProperty alias = new SimpleStringProperty();

    private OneWireContainer container;

    public String getDefaultPosition() {
        return defaultPosition.get();
    }

    public SimpleStringProperty defaultPositionProperty() {
        return defaultPosition;
    }

    public void setDefaultPosition(String defaultPosition) {
        this.defaultPosition.set(defaultPosition);
    }

    public String getSerial() {
        return serial.get();
    }

    public SimpleStringProperty serialProperty() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial.set(serial);
    }

    public String getModel() {
        return model.get();
    }

    public SimpleStringProperty modelProperty() {
        return model;
    }

    public void setModel(String model) {
        this.model.set(model);
    }

    public String getAlias() {
        return alias.get();
    }

    public SimpleStringProperty aliasProperty() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias.set(alias);
    }

    public OneWireContainer getContainer() {
        return container;
    }

    public void setContainer(OneWireContainer container) {
        this.container = container;
    }
}
