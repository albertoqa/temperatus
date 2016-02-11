package temperatus;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import temperatus.util.Constants;
import temperatus.util.SpringFxmlLoader;

/**
 * Created by alberto on 17/1/16.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.initStyle(StageStyle.UNDECORATED); // remove borders

        SpringFxmlLoader loader = new SpringFxmlLoader();
        Pane pane = loader.load(getClass().getResource(Constants.SPLASH));

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();

        //Search for connected devices and show a popup if detected
        /*new Thread() {
            // runnable for that thread
            public void run() {
                while (true) {
                    try {
                        for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements(); ) {
                            DSPortAdapter adapter = (DSPortAdapter) adapter_enum.nextElement();
                            for (Enumeration port_name_enum = adapter.getPortNames(); port_name_enum.hasMoreElements(); ) {
                                String port_name = (String) port_name_enum.nextElement();
                                try {
                                    adapter.selectPort(port_name);
                                    if (adapter.adapterDetected()) {
                                        adapter.beginExclusive(true);
                                        adapter.setSearchAllDevices();
                                        adapter.targetAllFamilies();
                                        for (Enumeration ibutton_enum = adapter.getAllDeviceContainers(); ibutton_enum.hasMoreElements(); ) {
                                            OneWireContainer ibutton = (OneWireContainer) ibutton_enum.nextElement();
                                            System.out.println(
                                                    adapter.getAdapterName() + "/" + port_name + "\t"
                                                            + ibutton.getName() + "\t"
                                                            + ibutton.getAddressAsString() + "\t"
                                                            + ibutton.getDescription().substring(0, 25) + "...");
                                        }
                                        adapter.endExclusive();
                                    }
                                    adapter.freePort();
                                } catch (Exception e) {
                                }
                                ;
                            }
                            System.out.println();
                        }
                        System.out.println();

                        // imitating work
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.start();*/

        // enumerate through each of the adapter classes
        /*for (Enumeration adapter_enum = OneWireAccessProvider.enumerateAllAdapters(); adapter_enum.hasMoreElements(); ) {

            // get the next adapter DSPortAdapter
            DSPortAdapter adapter = (DSPortAdapter) adapter_enum.nextElement();

            // get the port names we can use and try to open, test and close each
            for (Enumeration port_name_enum = adapter.getPortNames(); port_name_enum.hasMoreElements(); ) {

                // get the next packet
                String port_name = (String) port_name_enum.nextElement();
                try {
                    // select the port
                    adapter.selectPort(port_name);

                    // verify there is an adaptered detected
                    if (adapter.adapterDetected()) {
                        // added 8/29/2001 by SH
                        adapter.beginExclusive(true);

                        // clear any previous search restrictions
                        adapter.setSearchAllDevices();
                        adapter.targetAllFamilies();

                        // enumerate through all the iButtons found
                        for (Enumeration ibutton_enum = adapter.getAllDeviceContainers(); ibutton_enum.hasMoreElements(); ) {

                            // get the next ibutton
                            OneWireContainer ibutton = (OneWireContainer) ibutton_enum.nextElement();

                            System.out.println(
                                    adapter.getAdapterName() + "/" + port_name + "\t"
                                            + ibutton.getName() + "\t"
                                            + ibutton.getAddressAsString() + "\t"
                                            + ibutton.getDescription().substring(0, 25) + "...");
                        }

                        // added 8/29/2001 by SH
                        adapter.endExclusive();
                    }

                    // free this port
                    adapter.freePort();
                } catch (Exception e) {
                }
                ;
            }

            System.out.println();
        }

        System.out.println();*/


    }

}
