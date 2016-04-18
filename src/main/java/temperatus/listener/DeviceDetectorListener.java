package temperatus.listener;

import java.util.EventListener;

/**
 * Interface for classes which will listen for device changes (arrivals and departures)
 * <p>
 * If a class need to know about devices arrival/departure then it has to implement this interface
 * <p>
 * Created by alberto on 12/2/16.
 */
public interface DeviceDetectorListener extends EventListener {

    /**
     * Called when a new device is connected
     *
     * @param event info of the device arriving
     */
    void arrival(DeviceDetector event);

    /**
     * Called when a device is disconnected
     *
     * @param event info of the device departing
     */
    void departure(DeviceDetector event);

}
