package temperatus.listener;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by alberto on 12/2/16.
 */
@Component
public class DeviceDetectorTask implements Runnable {
    static Logger logger = Logger.getLogger(DeviceDetectorTask.class.getName());

    private List<String> serialsDetected = new ArrayList<>();

    @Autowired DeviceDetectorSource deviceDetectorSource;
    @Autowired DeviceSemaphore deviceSemaphore;

    @Override
    public void run() {
        try {
            logger.debug("Searching for connected devices... trying to acquire semaphore");
            deviceSemaphore.acquire();
            logger.debug("Semaphore adquired!");

            //searchForDevices();
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Semaphore released");
        }
    }

    private void searchForDevices() {
        logger.debug("Semaphore acquired...Performing search");

        for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements();) {
            DSPortAdapter adapter = (DSPortAdapter) adapter_enum.nextElement();

            for (Enumeration port_name_enum = adapter.getPortNames(); port_name_enum.hasMoreElements();) {
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
                        for (Enumeration ibutton_enum = adapter.getAllDeviceContainers(); ibutton_enum.hasMoreElements();) {
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
