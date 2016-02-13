package temperatus.listener;

import java.util.EventListener;

/**
 * Interface for classes which will listen for device changes (arrivals and departures)
 *
 * Created by alberto on 12/2/16.
 */
public interface DeviceDetectorListener extends EventListener {

    /**
     * Called when a new device is connected
     * @param event
     */
    public void arrival(DeviceDetector event);

    /**
     * Called when a device is disconnected
     * @param event
     */
    public void departure(DeviceDetector event);

}
