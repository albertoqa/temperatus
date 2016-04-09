package temperatus.listener;

import com.dalsemi.onewire.container.OneWireContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alberto on 12/2/16.
 */
@Component
public class DeviceDetectorSource {

    private List listeners = new ArrayList();

    public synchronized void addEventListener(DeviceDetectorListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeEventListener(DeviceDetectorListener listener) {
        listeners.remove(listener);
    }

    public synchronized void arrivalEvent(OneWireContainer container) {
        DeviceDetector event = new DeviceDetector(this);
        event.setContainer(container);
        event.setSerial(container.getAddressAsString());

        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((DeviceDetectorListener) i.next()).arrival(event);
        }
    }

    public synchronized void departureEvent(String serial) {
        DeviceDetector event = new DeviceDetector(this);
        event.setSerial(serial);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((DeviceDetectorListener) i.next()).departure(event);
        }
    }
}
