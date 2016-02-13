package temperatus.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alberto on 12/2/16.
 */
public class DeviceDetectorSource {

    private List listeners = new ArrayList();

    public synchronized void addEventListener(DeviceDetectorListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeEventListener(DeviceDetectorListener listener) {
        listeners.remove(listener);
    }

    public synchronized void arrivalEvent() {
        DeviceDetector event = new DeviceDetector(this);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((DeviceDetectorListener) i.next()).arrival(event);
        }
    }

    public synchronized void departureEvent() {
        DeviceDetector event = new DeviceDetector(this);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((DeviceDetectorListener) i.next()).departure(event);
        }
    }
}
