package temperatus.model.pojo.types;

import com.dalsemi.onewire.container.OneWireContainer;
import javafx.beans.property.SimpleStringProperty;

/**
 * Used to show connected devices in a tableView. When a new iButton is connected to the computer a new instance of this class
 * is created and added to the tableView of Connected Devices.
 * <p>
 * It's also used to pass the device to read tasks so we need to store the container and the info to regenerate the adapter.
 * <p>
 * Created by alberto on 13/2/16.
 */
public class Device {

    private SimpleStringProperty defaultPosition = new SimpleStringProperty();
    private SimpleStringProperty serial = new SimpleStringProperty();
    private SimpleStringProperty model = new SimpleStringProperty();
    private SimpleStringProperty alias = new SimpleStringProperty();

    private OneWireContainer container; // container referencing the device
    private String adapterName; // connected adapter name
    private String adapterPort; // port where the adapter is connected

    public Device(OneWireContainer container, String adapterName, String adapterPort) {
        this.serial.setValue(container.getAddressAsString());
        this.model.setValue(container.getName());
        this.container = container;
        this.adapterName = adapterName;
        this.adapterPort = adapterPort;
    }

    public String getDefaultPosition() {
        return defaultPosition.getValue();
    }

    public SimpleStringProperty defaultPositionProperty() {
        return defaultPosition;
    }

    public void setDefaultPosition(String defaultPosition) {
        this.defaultPosition.setValue(defaultPosition);
    }

    public String getSerial() {
        return serial.getValue();
    }

    public SimpleStringProperty serialProperty() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial.setValue(serial);
    }

    public String getModel() {
        return model.getValue();
    }

    public SimpleStringProperty modelProperty() {
        return model;
    }

    public void setModel(String model) {
        this.model.setValue(model);
    }

    public String getAlias() {
        return alias.getValue();
    }

    public SimpleStringProperty aliasProperty() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias.setValue(alias);
    }

    public OneWireContainer getContainer() {
        return container;
    }

    public void setContainer(OneWireContainer container) {
        this.container = container;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getAdapterPort() {
        return adapterPort;
    }

    public void setAdapterPort(String adapterPort) {
        this.adapterPort = adapterPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device)) return false;

        Device device = (Device) o;

        return getSerial() != null ? getSerial().equals(device.getSerial()) : device.getSerial() == null;

    }

    @Override
    public int hashCode() {
        return getSerial() != null ? getSerial().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getAlias() + " | " + getSerial();
    }
}
