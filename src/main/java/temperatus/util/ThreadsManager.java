package temperatus.util;

import org.apache.log4j.Logger;
import temperatus.listener.DaemonThreadFactory;
import temperatus.listener.DeviceDetectorTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by alberto on 16/2/16.
 */
public class ThreadsManager {

    static Logger logger = Logger.getLogger(ThreadsManager.class.getName());

    private static DaemonThreadFactory daemonThreadFactory = new DaemonThreadFactory();

    /**
     * Create a infinite task that search for all connected devices
     * If a new device is detected a notification is sent and all
     * classes which implement the DeviceDetectorListener are notified of
     * the event.
     * <p>
     * The task runs in a different thread and will stop when the program finish
     */
    public static void startDeviceListener() {
        logger.info("Starting device detector task");

        DeviceDetectorTask task = new DeviceDetectorTask();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);
        executor.scheduleAtFixedRate(task, Constants.DELAY, Constants.PERIOD, TimeUnit.SECONDS);
    }


}
