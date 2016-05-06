package temperatus.device;

import com.dalsemi.onewire.container.OneWireContainer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.types.Device;
import temperatus.model.service.IbuttonService;

/**
 * List of devices currently connected to the computer - shared by some views - synchronized
 * <p>
 * Created by alberto on 20/4/16.
 */
@Component
public class DeviceConnectedList implements DeviceDetectorListener {

    @Autowired IbuttonService ibuttonService;

    private DeviceConnectedList() {} // private constructor, class cannot be instantiated

    private ObservableList<Device> devices = FXCollections.observableArrayList();   // list of devices connected to the computer

    public synchronized ObservableList<Device> getDevices() {
        return devices;
    }

    public synchronized void setDevices(ObservableList<Device> devices) {
        this.devices = devices;
    }

    private synchronized void addDevice(Device device) {
        devices.add(device);
    }

    public synchronized Device getDevice(String serial) {
        for (Device device : devices) {
            if (device.getSerial().equals(serial)) {
                return device;
            }
        }
        return null;
    }

    /**
     * Search for the same device serial on the list and set its alias and defaultPosition
     * @param serial device's serial
     * @param alias alias to set
     * @param position position name to set
     */
    public synchronized void replaceDevice(String serial, String alias, String position) {
        devices.stream().filter(device -> device.getSerial().equals(serial)).forEach(device -> {
            device.setAlias(alias);
            device.setDefaultPosition(position);
        });
    }

    /**
     * On device arrival, create a new Device object with its information and search for it on the database
     * if it already exists set its alias and default position also. Finally add it to the list of connected
     * devices.
     *
     * @param event info of the device arriving
     */
    @Override
    public void arrival(DeviceDetector event) {
        OneWireContainer container = event.getContainer();
        Device device = new Device(container, event.getAdapterName(), event.getAdapterPort());

        Ibutton ibutton = ibuttonService.getBySerial(device.getSerial());
        if (ibutton != null) {
            device.setAlias(ibutton.getAlias());
            if (ibutton.getPosition() != null) {
                device.setDefaultPosition(ibutton.getPosition().getPlace());
            }
        }
        Platform.runLater(() -> addDevice(device));
    }

    /**
     * On device departure find it in the list of connected devices and remove it
     * If device was an adapter, remove all elements connected to it.
     *
     * @param event info of the device departing
     */
    @Override
    public void departure(DeviceDetector event) {
        String serial = event.getSerial();
        String port = event.getAdapterPort();
        String adapterName = event.getAdapterName();

        for (Device device : getDevices()) {
            if (serial.equals(device.getSerial()) || (adapterName.equals(device.getAdapterName()) && port.equals(device.getAdapterPort()))) {
                Platform.runLater(() -> getDevices().remove(device));
            }
        }
    }


}
