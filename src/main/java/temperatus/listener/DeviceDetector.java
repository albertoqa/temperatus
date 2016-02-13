package temperatus.listener;

import com.dalsemi.onewire.container.OneWireContainer;

import java.util.EventObject;

/**
 * Created by alberto on 12/2/16.
 */
public class DeviceDetector extends EventObject {

    private OneWireContainer container;
    private String serial;

    public DeviceDetector(Object source) {
        super(source);
    }

    public OneWireContainer getContainer() {
        return container;
    }

    public void setContainer(OneWireContainer container) {
        this.container = container;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}
