package temperatus.listener;

import com.dalsemi.onewire.container.OneWireContainer;

import java.util.EventObject;

/**
 * Event containing all the info of what happened - the device connected/disconnected
 * <p>
 * Created by alberto on 12/2/16.
 */
public class DeviceDetector extends EventObject {

    private OneWireContainer container;     // container of the connected device
    private String adapterName;
    private String adapterPort;
    private String serial;                  // serial of the connected device

    DeviceDetector(Object source) {
        super(source);
    }

    public OneWireContainer getContainer() {
        return container;
    }

    void setContainer(OneWireContainer container) {
        this.container = container;
    }

    public String getSerial() {
        return serial;
    }

    void setSerial(String serial) {
        this.serial = serial;
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
}
