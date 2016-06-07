package temperatus.device.task;

import com.dalsemi.onewire.container.MissionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Disable (if currently active) a device's mission
 * <p>
 * Created by alberto on 18/4/16.
 */
public class DeviceMissionDisableTask extends DeviceTask {

    private static Logger logger = LoggerFactory.getLogger(DeviceMissionDisableTask.class.getName());

    @Override
    public Boolean call() throws Exception {
        try {
            logger.info("Disabling device's mission...");
            deviceSemaphore.acquire();
            logger.debug("Disable mission Semaphore acquired!");

            return disableMission();

        } catch (InterruptedException e) {
            logger.error("Error with the semaphore in DeviceMissionDisableTask");
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Disable mission Semaphore released");
        }
    }

    /**
     * If the device has an active mission this function stops it.
     *
     * @return success?
     */
    private boolean disableMission() {
        try {
            setUpAdapter();

            if (((MissionContainer) container).isMissionRunning()) {    // check if there is a mission running
                ((MissionContainer) container).stopMission();           // disable current mission
                logger.info("Mission disabled successfully");
            } else {
                logger.info("No mission in progress");
            }
            return true;
        } catch (Exception e) {
            logger.error("Cannot disable mission: " + e.getMessage());
            return false;
        } finally {
            releaseAdapter();
        }
    }

}
