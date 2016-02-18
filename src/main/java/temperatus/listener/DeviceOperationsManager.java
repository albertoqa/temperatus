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

    public void init() {
        operationsExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory);
        scanShedulerExecutor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);

        scanShedulerExecutor.scheduleAtFixedRate(scanTask, Constants.DELAY, Constants.PERIOD, TimeUnit.SECONDS);
    }

    public <T> Future<T> submitReadTask(DeviceReadTask readTask) {
        return operationsExecutor.submit(readTask);
    }

}
