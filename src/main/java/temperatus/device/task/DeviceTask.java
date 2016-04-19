package temperatus.device.task;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import temperatus.device.DeviceSemaphore;
import temperatus.exception.ControlledTemperatusException;

import java.util.concurrent.Callable;

/**
 * Abstract class for task over a device
 * <p/>
 * Created by alberto on 18/4/16.
 */
public abstract class DeviceTask implements Callable {

    private static Logger logger = LoggerFactory.getLogger(DeviceTask.class.getName());

    @Autowired DeviceSemaphore deviceSemaphore; // shared semaphore

    OneWireContainer container = null;
    DSPortAdapter adapter = null;
    String adapterName = null;
    String adapterPort = null;

    void setUpAdapter() throws ControlledTemperatusException {
        try {
            adapter = OneWireAccessProvider.getAdapter(adapterName, adapterPort);

            if (container == null || adapter == null) {
                logger.warn("Error reading temperature, container is null");
                throw new ControlledTemperatusException("Container is null, cannot read temperature");
            }

        } catch (OneWireException e) {
            e.printStackTrace();
        }

        try {
            adapter.selectPort(adapterPort);
            adapter.beginExclusive(true);
            container.setupContainer(adapter, container.getAddress());
            logger.debug("Adapter is exclusive now, reading device's temperature");

        } catch (Exception e) {
            throw new ControlledTemperatusException("Error while reading temperature." + e.getMessage());
        }
    }

    void releaseAdapter() {
        adapter.endExclusive();
        try {
            adapter.freePort();
        } catch (OneWireException ex) {
            logger.error("Error closing port");
        }
        logger.debug("Adapter end exclusivity");
    }

    /**
     * Set container to read temperature from
     *
     * @param container device container
     */
    public void setContainer(OneWireContainer container, String adapterName, String adapterPort) {
        if (container != null && adapterName != null && adapterPort != null) {
            this.container = container;
            this.adapterPort = adapterPort;
            this.adapterName = adapterName;
        }
    }

}
