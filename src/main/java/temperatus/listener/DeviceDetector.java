package temperatus.listener;

import java.util.EventObject;

/**
 * Created by alberto on 12/2/16.
 */
public class DeviceDetector extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public DeviceDetector(Object source) {
        super(source);
    }
}
