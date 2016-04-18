package temperatus.device.task;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import temperatus.exception.ControlledTemperatusException;

/**
 * Read in real time the current temperature from the device.
 * Return the temperature in celsius (double)
 * <p>
 * Created by alberto on 18/4/16.
 */
@Component
public class DeviceRealTimeTempTask extends DeviceTask {

    private static Logger logger = LoggerFactory.getLogger(DeviceRealTimeTempTask.class.getName());

    private TemperatureContainer container = null;
    private DSPortAdapter adapter = null;

    @Override
    public Double call() throws Exception {
        try {
            logger.info("Getting current temperature from device...");
            deviceSemaphore.acquire();
            logger.debug("Real-time temperature Semaphore adquired!");

            //return 0.0;
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

        if (container == null || adapter == null) {
            logger.warn("Error reading temperature, container is null");
            throw new ControlledTemperatusException("Container is null, cannot read temperature");
        }

        double currentTemp = Double.NaN;

        try {
            adapter.beginExclusive(true);
            logger.debug("Adapter is exclusive now, reading device's temperature");

            byte[] state = container.readDevice();
            container.doTemperatureConvert(state);
            currentTemp = container.getTemperature(state);
            logger.info("Temperature (celsius) read: " + currentTemp);

        } catch (Exception e) {
            throw new ControlledTemperatusException("Error while reading temperature." + e.getMessage());
        } finally {
            adapter.endExclusive();
            logger.debug("Adapter end exclusivity");
        }

        return currentTemp;
    }

    /**
     * Set container to read temperature from
     *
     * @param container device container
     */
    public void setContainer(OneWireContainer container) {
        if (container != null) {
            this.container = (TemperatureContainer) container;
            this.adapter = container.getAdapter();
        }
    }

}
