package temperatus.device.task;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.device.DeviceSemaphore;
import temperatus.listener.DeviceDetectorSource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by alberto on 12/2/16.
 */
@Component
public class DeviceDetectorTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(DeviceDetectorTask.class.getName());

    private List<String> serialsDetected = new ArrayList<>();

    @Autowired DeviceDetectorSource deviceDetectorSource;
    @Autowired DeviceSemaphore deviceSemaphore;

    @Override
    public void run() {
        try {
            logger.info("Searching for connected devices...");
            deviceSemaphore.acquire();
            logger.debug("Scan Semaphore adquired!");

            searchForDevices();
            //Thread.sleep(2000);

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Scan Semaphore released");
        }
    }

    private void searchForDevices() {
        logger.debug("Semaphore acquired...Performing search");

        for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements(); ) {
            DSPortAdapter adapter = (DSPortAdapter) adapter_enum.nextElement();

            for (Enumeration port_name_enum = adapter.getPortNames(); port_name_enum.hasMoreElements(); ) {
                String port_name = (String) port_name_enum.nextElement();

                try {
                    adapter.selectPort(port_name);
                    if (adapter.adapterDetected()) {

                        for (String serial : serialsDetected) {
                            if (!adapter.isPresent(serial)) {
                                serialsDetected.remove(serial);
                                deviceDetectorSource.departureEvent(serial);
                            }
                        }

                        adapter.beginExclusive(true);
                        adapter.setSearchAllDevices();
                        adapter.targetAllFamilies();
                        for (Enumeration ibutton_enum = adapter.getAllDeviceContainers(); ibutton_enum.hasMoreElements(); ) {
                            OneWireContainer ibutton = (OneWireContainer) ibutton_enum.nextElement();
                            String serial = ibutton.getAddressAsString();

                            if (!serialsDetected.contains(serial)) {
                                serialsDetected.add(serial);
                                deviceDetectorSource.arrivalEvent(ibutton);
                            }
                        }
                        adapter.endExclusive();
                    }
                    adapter.freePort();

                } catch (Exception e) {
                    // only prevent exception, not manage it at all... ¿NetAdapter?
                } finally {
                    try {
                        adapter.endExclusive();
                        adapter.freePort();
                    } catch (OneWireException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        logger.debug("Search finished");
    }


}
