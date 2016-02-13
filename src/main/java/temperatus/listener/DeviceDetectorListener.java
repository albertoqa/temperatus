package temperatus.listener;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Created by alberto on 12/2/16.
 */
public interface DeviceDetectorListener extends EventListener {

    public void deviceDetected(EventObject event);

}
