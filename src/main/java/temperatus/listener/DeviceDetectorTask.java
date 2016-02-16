package temperatus.listener;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer;
import org.apache.log4j.Logger;
import temperatus.util.Constants;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by alberto on 12/2/16.
 */
public class DeviceDetectorTask implements Runnable {

    static Logger logger = Logger.getLogger(DeviceDetectorTask.class.getName());

    List<String> serialsDetected = new ArrayList<>();
    List<DSPortAdapter> adaptersDetected = new ArrayList<>();

    @Override
    public void run() {

        logger.debug("Searching for connected devices...");

        for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements(); ) {
            DSPortAdapter adapter = (DSPortAdapter) adapter_enum.nextElement();

            // To prevent two processes trying to get access to an iButton at the same time
            // i have to check if it has been already detected and is still connected.

            // DSPortAdapter equals compare {adapterName portName}
            if(!adaptersDetected.contains(adapter)) {   // TODO - ¿esto esta bien?
                for (Enumeration port_name_enum = adapter.getPortNames(); port_name_enum.hasMoreElements(); ) {
                    String port_name = (String) port_name_enum.nextElement();

                    try {
                        adapter.selectPort(port_name);
                        if (adapter.adapterDetected()) {

                            boolean isInTheList = false;

                            for (String serial : serialsDetected) {
                                if (!adapter.isPresent(serial)) {
                                    serialsDetected.remove(serial);
                                    Constants.deviceDetectorSource.departureEvent(serial);
                                } else {
                                    isInTheList = true;
                                }
                            }

                            if (!isInTheList) {
                                adapter.beginExclusive(true);
                                adapter.setSearchAllDevices();
                                adapter.targetAllFamilies();
                                for (Enumeration ibutton_enum = adapter.getAllDeviceContainers(); ibutton_enum.hasMoreElements(); ) {
                                    OneWireContainer ibutton = (OneWireContainer) ibutton_enum.nextElement();
                                    String serial = ibutton.getAddressAsString();

                                    if (!serialsDetected.contains(serial)) {
                                        serialsDetected.add(serial);
                                        Constants.deviceDetectorSource.arrivalEvent(ibutton);
                                    }

                                }
                                adapter.endExclusive();
                            }
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
        }
    }
}
