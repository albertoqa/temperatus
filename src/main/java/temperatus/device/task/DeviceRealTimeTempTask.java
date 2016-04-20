package temperatus.device.task;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.TemperatureContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;

/**
 * Read in real time the current temperature from the device.
 * Return the temperature in celsius (double)
 * <p/>
 * Created by alberto on 18/4/16.
 */
@Component
public class DeviceRealTimeTempTask extends DeviceTask {

    private static Logger logger = LoggerFactory.getLogger(DeviceRealTimeTempTask.class.getName());

    @Override
    public Double call() throws Exception {
        try {
            logger.info("Getting current temperature from device...");
            deviceSemaphore.acquire();
            logger.debug("Real-time temperature Semaphore adquired!");

            return getCurrentTemperature();

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Real-time temperature Semaphore released");
        }
    }

    /**
     * Read from device the current temperature in celsius
     *
     * @return current temperature
     * @throws ControlledTemperatusException
     */
    private double getCurrentTemperature() throws ControlledTemperatusException {
        try {
            setUpAdapter();

            byte[] state = ((TemperatureContainer) container).readDevice();
            ((TemperatureContainer) container).doTemperatureConvert(state);
            double currentTemp = ((TemperatureContainer) container).getTemperature(state);

            logger.debug("Temperature (celsius) read: " + currentTemp);
            return currentTemp;

        } catch (OneWireException e) {
            logger.error("Error reading temperature:  " + e.getMessage());
            throw new ControlledTemperatusException("Error while reading temperature." + e.getMessage());
        } finally {
            releaseAdapter();
        }
    }

}
