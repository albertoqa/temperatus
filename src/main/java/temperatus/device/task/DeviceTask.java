package temperatus.device.task;

import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import temperatus.device.DeviceSemaphore;

/**
 * Abstract class for task over a device
 * <p>
 * Created by alberto on 18/4/16.
 */
public abstract class DeviceTask extends Task {

    @Autowired DeviceSemaphore deviceSemaphore; // shared semaphore

}
