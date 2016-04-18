package temperatus.device.task;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.MissionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by alberto on 18/4/16.
 */
@Component
@Scope("prototype")
public class DeviceMissionDisableTask extends DeviceTask {

    private DSPortAdapter adapter = null;
    private MissionContainer container = null;

    private static Logger logger = LoggerFactory.getLogger(DeviceMissionDisableTask.class.getName());

    @Override
    public Boolean call() throws Exception {
        try {
            logger.info("Disabling device's mission...");
            deviceSemaphore.acquire();
            logger.debug("Disable mission Semaphore adquired!");

            disableMission();

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Disable mission Semaphore released");
        }

        return null;
    }

    private boolean disableMission() {
        if (adapter == null || container == null) {
            logger.error("Error: Adapter or container are null");
            return false;
        }
        try {
            adapter.beginExclusive(true);

            if (container.isMissionRunning()) {
                container.stopMission();    // disable current mission
                logger.info("Mission disabled successfully");
            } else {
                logger.info("No mission in progress");
            }
            return true;

        } catch (Exception e) {
            logger.error("Cannot disable mission: " + e.getMessage());
            return false;
        } finally {
            adapter.endExclusive();
        }
    }

    public void setAdapter(DSPortAdapter adapter) {
        this.adapter = adapter;
        this.container = (MissionContainer) adapter.getDeviceContainer();
    }
}
