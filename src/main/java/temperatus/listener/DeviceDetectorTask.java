package temperatus.listener;

import com.dalsemi.onewire.OneWireAccessProvider;
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

    @Override
    public void run() {

        logger.debug("Searching for connected devices...");

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
                                Constants.deviceDetectorSource.departureEvent();
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
                                Constants.deviceDetectorSource.arrivalEvent();
                            }

                        }
                        adapter.endExclusive();
                    }
                    adapter.freePort();

                } catch (Exception e) {
                    // only prevent exception, not manage it at all... ¿NetAdapter?
                }
            }
        }
    }
}