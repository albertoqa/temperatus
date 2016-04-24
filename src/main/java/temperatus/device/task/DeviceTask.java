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


//###################################################################################
//                                                                                  #
//                              GENERAL INFO                                        #
//                                                                                  #
//###################################################################################
/*

    ------- Every call override must follow this pattern ------

    @Override
    public T call() throws Exception {
        try {
            deviceSemaphore.acquire();

            return operation();

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
        }
    }

    ------- Every device operation must follow this pattern -------

    private double operation() throws ControlledTemperatusException {
        try {
            setUpAdapter();

            return operate();

        } catch (OneWireException e) {
            throw new ControlledTemperatusException("Error" + e.getMessage());
        } finally {
            releaseAdapter();
        }

//###################################################################################

*/

/**
 * Abstract class for task over a device
 * <p>
 * Created by alberto on 18/4/16.
 */
public abstract class DeviceTask implements Callable {

    private static Logger logger = LoggerFactory.getLogger(DeviceTask.class.getName());

    @Autowired DeviceSemaphore deviceSemaphore; // shared semaphore

    public OneWireContainer container = null;
    private DSPortAdapter adapter = null;
    private String adapterName = null;
    private String adapterPort = null;
    boolean saveToFile = false;

    /**
     * Set up the adapter and container, select the port and start adapter's exclusivity
     * This must be called before read/write from/to the device
     *
     * @throws ControlledTemperatusException
     */
    void setUpAdapter() throws ControlledTemperatusException {
        try {
            adapter = OneWireAccessProvider.getAdapter(adapterName, adapterPort);   // get the adapter from the previously saved info

            if (container == null || adapter == null) {
                logger.error("Error setting up adapter. Container or adapter is null");
                throw new ControlledTemperatusException("Container or adapter is null. Cannot set up adapter");
            }

            adapter.selectPort(adapterPort);    // get the port, from now on the port is exclusive
            adapter.beginExclusive(true);       // begin adapter's exclusivity
            container.setupContainer(adapter, container.getAddress());  // set up the adapter in the device's container
            logger.debug("Adapter is exclusive now");

        } catch (OneWireException e) {
            throw new ControlledTemperatusException("Error while setting up Container/Adapter.  " + e.getMessage());
        }

    }

    /**
     * Free the port and end adapter exclusivity
     * This function must be called inside the finally {} part
     */
    void releaseAdapter() {
        try {
            adapter.endExclusive();
            adapter.freePort();
        } catch (OneWireException ex) {
            logger.error("Error freeing port or ending adapter's exclusivity.  " + ex.getMessage());
        }
        logger.debug("Adapter end exclusivity successfully.");
    }


    /**
     * Set device's data to read from
     *
     * @param container   device's container
     * @param adapterName adapter where the device is connected name
     * @param adapterPort port where the adapter is connected
     * @param saveToFile  save information to a file
     */
    public void setDeviceData(OneWireContainer container, String adapterName, String adapterPort, boolean saveToFile) {
        this.container = container;
        this.adapterPort = adapterPort;
        this.adapterName = adapterName;
        this.saveToFile = saveToFile;
    }

}
