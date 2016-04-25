package temperatus.device.task;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import javafx.util.Pair;
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
 * Keep a list with all connected devices.
 * Iterate over all possible adapters and ports looking for new connected devices.
 * When a new event (arrival/departure) is detected, the list is updated and all registered listeners are warned of the event.
 * <p>
 * This task runs every X seconds.
 * <p>
 * Created by alberto on 12/2/16.
 */
@Component
public class DeviceDetectorTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(DeviceDetectorTask.class.getName());

    private List<String> serialsDetected = new ArrayList<>();   // list of device's serials currently connected (already detected)
    private List<Pair<String, String>> devicesDetected = new ArrayList<>();

    @Autowired DeviceDetectorSource deviceDetectorSource;   // create event to let all listeners know about what is happening
    @Autowired DeviceSemaphore deviceSemaphore;             // shared semaphore

    @Override
    public void run() {
        try {
            logger.info("Searching for connected devices...");
            deviceSemaphore.acquire();
            logger.debug("Scan Semaphore adquired!");

            searchForDevices();

        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } finally {
            deviceSemaphore.release();
            logger.debug("Scan Semaphore released");
        }
    }

    /**
     * Iterate over all possible adapters and ports looking for connected devices
     */
    private void searchForDevices() {
        logger.debug("Semaphore acquired...Performing search");

        for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements(); ) {
            DSPortAdapter adapter = (DSPortAdapter) adapter_enum.nextElement();

            for (Enumeration port_name_enum = adapter.getPortNames(); port_name_enum.hasMoreElements(); ) {
                String port_name = (String) port_name_enum.nextElement();

                try {
                    adapter.selectPort(port_name);
                    String adapterNamePort = adapter.getAdapterName() + "-" + adapter.getPortName();
                    if (adapter.adapterDetected()) {

                        // Check if devices previously detected are still connected
                        /*for (String serial : serialsDetected) {
                            if (!adapter.isPresent(serial)) {
                                serialsDetected.remove(serial);
                                deviceDetectorSource.departureEvent(serial);
                            }
                        }*/

                        adapter.beginExclusive(true);
                        adapter.setSearchAllDevices();
                        adapter.targetAllFamilies();
                        for (Enumeration containers = adapter.getAllDeviceContainers(); containers.hasMoreElements(); ) {
                            OneWireContainer container = (OneWireContainer) containers.nextElement();
                            String serial = container.getAddressAsString();

                            // New device detected, add it to the list and warn the listeners
                            /*if (!serialsDetected.contains(serial)) {
                                serialsDetected.add(serial);
                                deviceDetectorSource.arrivalEvent(container, adapter.getAdapterName(), adapter.getPortName());
                            }*/

                            for(Pair<String, String> device: devicesDetected) {
                                if(!adapter.isPresent(device.getKey())) {
                                    serialsDetected.remove(device.getKey());
                                    deviceDetectorSource.departureEvent(device.getKey());
                                }
                            }

                            Pair<String, String> device = new Pair<>(serial, adapterNamePort);
                            if(!devicesDetected.contains(device)) {
                                devicesDetected.add(device);
                                deviceDetectorSource.arrivalEvent(container, adapter.getAdapterName(), adapter.getPortName());
                            }

                        }
                        adapter.endExclusive();
                    } else {
                        /*for(Pair<String, String> device: devicesDetected) {
                            if(device.getValue().equals(adapterNamePort)) {

                            }
                            // este adaptador no esta conectado... si tenía dispositivos conectados a el esos el programa lso reconoce como conectados
                        }*/
                    }
                    adapter.freePort();

                } catch (Exception e) {
                    // only prevent exception, not manage it at all... ¿NetAdapter?
                    // logger.debug("Exception in scan...  " + e.getMessage());
                } finally {
                    try {
                        adapter.endExclusive();
                        adapter.freePort();
                    } catch (OneWireException e) {
                        logger.debug("Error closing ports... " + e.getMessage());
                    }
                }
            }
        }

        logger.debug("Search finished");
    }


}
