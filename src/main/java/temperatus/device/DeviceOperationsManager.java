package temperatus.device;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.device.task.DeviceDetectorTask;
import temperatus.device.task.DeviceTask;
import temperatus.listener.DaemonThreadFactory;
import temperatus.util.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Control all operations related to devices.
 * There are two executors with only one thread each. The first one will hold the device read tasks (read from device).
 * The second one is a scheduled executor which runs a scan task every PERIOD seconds.
 * <p>
 * Both tasks (scan and read) are required to get a semaphore before perform the operation over the device so only one
 * of them will be active at a time.
 *
 * @author aquesada
 */
@Component
public class DeviceOperationsManager {

    private DaemonThreadFactory daemonThreadFactory = new DaemonThreadFactory();    // stop thread on application exit

    private ListeningExecutorService operationsExecutor;        // Read/Write tasks executor
    private ScheduledExecutorService scanSchedulerExecutor;     // Scan tasks executor

    @Autowired DeviceDetectorTask scanTask;                     // Scan task

    public void init() {
        operationsExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(daemonThreadFactory));

        scanSchedulerExecutor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);
        scanSchedulerExecutor.scheduleAtFixedRate(scanTask, Constants.DELAY, Constants.PERIOD, TimeUnit.SECONDS);
    }

    /**
     * Submit a new task to the operationsExecutor
     * The future's data will be available once the task has been complete.
     *
     * @param deviceTask task to submit
     */
    public <T> ListenableFuture submitTask(DeviceTask deviceTask) {
        return operationsExecutor.submit(deviceTask);
    }

}
