package temperatus.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.util.Constants;

import java.util.concurrent.*;

/**
 * @author aquesada
 */
@Component
public class DeviceOperationsManager {

    private DaemonThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    private ExecutorService operationsExecutor;
    private ScheduledExecutorService scanShedulerExecutor;

    @Autowired DeviceDetectorTask scanTask;

    /**
     * Create a infinite task that search for all connected devices
     * If a new device is detected a notification is sent and all
     * classes which implement the DeviceDetectorListener are notified of
     * the event.
     * <p>
     * The task runs in a different thread and will stop when the program finish
     */
    public void init() {
        operationsExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        scanShedulerExecutor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);

        scanShedulerExecutor.scheduleAtFixedRate(scanTask, Constants.DELAY, Constants.PERIOD, TimeUnit.SECONDS);
    }

    public <T> Future<T> submitReadTask(DeviceReadTask readTask) {
        return operationsExecutor.submit(readTask);
    }


}
