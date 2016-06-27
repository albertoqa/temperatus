package temperatus.device.task;

import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.OneWireContainer41;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.model.pojo.Configuration;

/**
 * Start a mission on the device with the passed configuration
 * <p>
 * Created by alberto on 19/4/16.
 */
public class DeviceMissionStartTask extends DeviceTask {

    private Configuration configuration;

    private static Logger logger = LoggerFactory.getLogger(DeviceMissionStartTask.class.getName());

    @Override
    public Boolean call() throws Exception {
        try {
            logger.info("Start device's mission...");
            deviceSemaphore.acquire();
            logger.debug("Start mission Semaphore acquired!");

            return startMission();

        } catch (InterruptedException e) {
            logger.error("Error with the semaphore in DeviceMissionStartTask");
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Start mission Semaphore released");
        }
    }

    /**
     * Activate a mission on the device getting the configuration from the user input or database
     *
     * @return successfully started mission?
     */
    private boolean startMission() {
        try {
            setUpAdapter();

            MissionContainer missionContainer = (MissionContainer) container;

            // disable current mission if active
            if (missionContainer.isMissionRunning()) {
                missionContainer.stopMission();
            }

            // clear memory contents
            missionContainer.clearMissionResults();

            // check if necessary to activate any alarm
            boolean anyAlarmsEnabled = false;

            if (configuration.getEnableAlarmC1() != null && configuration.getHighAlarmC1() != null && configuration.getLowAlarmC1() != null) {
                if (configuration.getEnableAlarmC1()) {
                    anyAlarmsEnabled = true;
                    missionContainer.setMissionAlarm(0, MissionContainer.ALARM_HIGH, configuration.getHighAlarmC1());
                    missionContainer.setMissionAlarm(0, MissionContainer.ALARM_LOW, configuration.getLowAlarmC1());
                }

                missionContainer.setMissionAlarmEnable(0, MissionContainer.ALARM_HIGH, configuration.getEnableAlarmC1());
                missionContainer.setMissionAlarmEnable(0, MissionContainer.ALARM_LOW, configuration.getEnableAlarmC1());
                missionContainer.setMissionResolution(0, configuration.getResolutionC1());
            }

            if (configuration.getEnableAlarmC2() != null && configuration.getHighAlarmC2() != null && configuration.getLowAlarmC2() != null) {
                if (configuration.getEnableAlarmC2()) {
                    anyAlarmsEnabled = true;
                    missionContainer.setMissionAlarm(1, MissionContainer.ALARM_HIGH, configuration.getHighAlarmC2());
                    missionContainer.setMissionAlarm(1, MissionContainer.ALARM_LOW, configuration.getLowAlarmC2());
                }

                missionContainer.setMissionAlarmEnable(1, MissionContainer.ALARM_HIGH, configuration.getEnableAlarmC2());
                missionContainer.setMissionAlarmEnable(1, MissionContainer.ALARM_LOW, configuration.getEnableAlarmC2());
                missionContainer.setMissionResolution(1, configuration.getResolutionC2());
            }

            boolean[] channelEnabled = {configuration.getChannelEnabledC1(), configuration.getChannelEnabledC2()};

            ((OneWireContainer41) missionContainer).setStartUponTemperatureAlarmEnable(configuration.isSuta() && anyAlarmsEnabled);
            missionContainer.startNewMission(configuration.getRate(), configuration.getDelay(), configuration.isRollover(), configuration.isSyncTime(), channelEnabled);

            return true;
        } catch (Exception e) {
            logger.error("Cannot start mission: " + e.getMessage());
            return false;
        } finally {
            releaseAdapter();
        }
    }

    /**
     * Set the configuration to use to configure the device
     *
     * @param configuration configuration to use
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
