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

    // call this method whenever you want to notify
    //the event listeners of the particular event
    public synchronized void fireEvent() {
        DeviceDetector event = new DeviceDetector(this);
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
            ((DeviceDetectorListener) i.next()).deviceDetected(event);
        }
    }
}
