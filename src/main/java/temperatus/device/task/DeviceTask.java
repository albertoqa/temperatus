package temperatus.device.task;

import org.springframework.beans.factory.annotation.Autowired;
import temperatus.device.DeviceSemaphore;

import java.util.concurrent.Callable;

/**
 * Abstract class for task over a device
 * <p>
 * Created by alberto on 18/4/16.
 */
public abstract class DeviceTask implements Callable {

    @Autowired DeviceSemaphore deviceSemaphore; // shared semaphore

}
