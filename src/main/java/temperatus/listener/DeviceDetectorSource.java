package temperatus.listener;

import com.dalsemi.onewire.container.OneWireContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Component generator of events. It let know to all the listeners that something (arrival/departure) has happened.
 * If a class needs to know about device's connections, it must implement DeviceDetectorListener interface and be
 * added to the listener's list.
 * <p>
 * Singleton instance of this class is shared by all the application.
 * <p>
 * Created by alberto on 12/2/16.
 */
@Component
//@Scope("prototype")   // TODO ¿should it be singleton?
public class DeviceDetectorSource {

    private static Logger logger = LoggerFactory.getLogger(DeviceDetectorSource.class.getName());

    /**
     * List of observers/listeners to notify of any change in the state
     */
    private List<DeviceDetectorListener> listeners = new ArrayList<>();

    /**
     * Register a new observer/listener
     *
     * @param listener new listener to include in the list
     */
    public synchronized void addEventListener(DeviceDetectorListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister a observer/listener
     *
     * @param listener listener to exclude from the list
     */
    public synchronized void removeEventListener(DeviceDetectorListener listener) {
        listeners.remove(listener);
    }

    /**
     * Fire a action in each registered listener to let it know that a device has been connected
     *
     * @param container device's container
     */
    public synchronized void arrivalEvent(OneWireContainer container, String adapterName, String adapterPort) {
        logger.info("Arrival event \n Notifying of the arrival to all the listeners");

        DeviceDetector event = new DeviceDetector(this);
        event.setContainer(container);
        event.setSerial(container.getAddressAsString());
        event.setAdapterName(adapterName);
        event.setAdapterPort(adapterPort);

        for (DeviceDetectorListener listener : listeners) {
            (listener).arrival(event);
        }
    }

    /**
     * Fire a action in each registered listener to let it know that a device has been disconnected
     *
     * @param serial device's serial
     */
    public synchronized void departureEvent(String serial) {
        logger.info("Departure event with serial: " + serial + "\n Notifying of the departure to all the listeners");

        DeviceDetector event = new DeviceDetector(this);
        event.setSerial(serial);

        for (DeviceDetectorListener listener : listeners) {
            (listener).departure(event);
        }
    }
}
