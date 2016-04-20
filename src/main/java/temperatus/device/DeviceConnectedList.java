package temperatus.device;

import com.dalsemi.onewire.container.OneWireContainer;
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
 * Created by alberto on 20/4/16.
 */
@Component
public class DeviceConnectedList implements DeviceDetectorListener {

    @Autowired IbuttonService ibuttonService;

    private DeviceConnectedList() {
    }

    private ObservableList<Device> devices = FXCollections.observableArrayList();

    public synchronized ObservableList<Device> getDevices() {
        return devices;
    }

    public synchronized void setDevices(ObservableList<Device> devices) {
        this.devices = devices;
    }

    public synchronized void addDevice(Device device) {
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

    @Override
    public void arrival(DeviceDetector event) {
        OneWireContainer container = event.getContainer();
        String adapterName = event.getAdapterName();
        String adapterPort = event.getAdapterPort();

        String model = container.getName();
        String serial = container.getAddressAsString();
        String alternateNames = container.getAlternateNames();

        Device device = new Device();
        device.setContainer(container);
        device.setModel(model);
        device.setSerial(serial);
        device.setAdapterName(adapterName);
        device.setAdapterPort(adapterPort);

        Ibutton ibutton = ibuttonService.getBySerial(device.getSerial());

        if (ibutton != null) {
            device.setAlias(ibutton.getAlias());

            if (ibutton.getPosition() != null) {
                device.setDefaultPosition(ibutton.getPosition().getPlace());
            }
        }

        devices.add(device);
    }

    @Override
    public void departure(DeviceDetector event) {

    }

}
