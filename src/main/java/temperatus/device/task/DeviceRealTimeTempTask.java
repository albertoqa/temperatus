package temperatus.device.task;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
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

    private OneWireContainer container = null;
    private DSPortAdapter adapter = null;
    private String adapterName = null;
    private String adapterPort = null;

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
    private double getCurrentTemperature() throws ControlledTemperatusException, OneWireException {

        adapter = OneWireAccessProvider.getAdapter(adapterName, adapterPort);

        if (container == null || adapter == null) {
            logger.warn("Error reading temperature, container is null");
            throw new ControlledTemperatusException("Container is null, cannot read temperature");
        }

        double currentTemp = Double.NaN;

        try {
            adapter.selectPort(adapterPort);
            adapter.beginExclusive(true);
            container.setupContainer(adapter, container.getAddress());
            logger.debug("Adapter is exclusive now, reading device's temperature");

            byte[] state = ((TemperatureContainer) container).readDevice();
            ((TemperatureContainer) container).doTemperatureConvert(state);
            currentTemp = ((TemperatureContainer) container).getTemperature(state);
            logger.info("Temperature (celsius) read: " + currentTemp);

        } catch (Exception e) {
            throw new ControlledTemperatusException("Error while reading temperature." + e.getMessage());
        } finally {
            adapter.endExclusive();
            try {
                adapter.freePort();
            } catch (OneWireException ex) {
                logger.error("Error closing port");
            }
            logger.debug("Adapter end exclusivity");
        }

        return currentTemp;
    }

    /**
     * Set container to read temperature from
     *
     * @param container device container
     */
    public void setContainer(OneWireContainer container, String adapterName, String adapterPort) {
        if (container != null && adapterName != null && adapterPort != null) {
            this.container =  container;
            this.adapterPort = adapterPort;
            this.adapterName = adapterName;
        }
    }

}
