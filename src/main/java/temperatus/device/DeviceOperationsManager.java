package temperatus.device;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.device.task.DeviceDetectorTask;
import temperatus.device.task.DeviceTask;

import java.util.concurrent.*;

/**
 * Control all operations related to devices.
 * There are two executors with only one thread each. The first one will hold the device read/write tasks (read/write from/to device).
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

    private static final int DELAY = 0;    // DeviceDetectorTask delay in seconds
    private static final int PERIOD = 8;   // DeviceDetectorTask run period (s)

    @Autowired DeviceDetectorTask scanTask;                     // Scan task

    public void init() {
        operationsExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(daemonThreadFactory));

        scanSchedulerExecutor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);

        // don't submit a scan task until the last execution is completed
        scanSchedulerExecutor.scheduleAtFixedRate(new Runnable() {
            private final ExecutorService executor = Executors.newSingleThreadExecutor();
            private Future<?> lastExecution;
            @Override
            public void run() {
                if (lastExecution != null && !lastExecution.isDone()) {
                    return;
                }
                lastExecution = executor.submit(scanTask);
            }
        }, DELAY, PERIOD, TimeUnit.MINUTES);

        //scanSchedulerExecutor.scheduleAtFixedRate(scanTask, DELAY, PERIOD, TimeUnit.SECONDS);
    }

    /**
     * Submit a new task to the operationsExecutor
     * The future's data will be available once the task has been complete.
     *
     * @param deviceTask task to submit
     */
    public ListenableFuture submitTask(DeviceTask deviceTask) {
        return operationsExecutor.submit(deviceTask);
    }

}
