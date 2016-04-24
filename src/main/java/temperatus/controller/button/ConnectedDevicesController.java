package temperatus.controller.button;

import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.types.Device;
import temperatus.model.service.IbuttonService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show all the devices connected to the computer and their information. Depending on
 * the type of device the information shown will be direfert.
 * <p>
 * Created by alberto on 23/1/16.
 */
@Controller
public class ConnectedDevicesController implements Initializable, AbstractController, DeviceDetectorListener {

    @FXML private Label headerLabel;
    @FXML private Label searchingLabel;

    @FXML private TabPane infoTabPane;
    @FXML private TableView<Device> connectedDevicesTable;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private HBox searchingIndicator;

    private TableColumn<Device, String> modelColumn = new TableColumn<>();
    private TableColumn<Device, String> serialColumn = new TableColumn<>();
    private TableColumn<Device, String> aliasColumn = new TableColumn<>();
    private TableColumn<Device, String> positionColumn = new TableColumn<>();

    private ObservableList<Device> devicesConnected = FXCollections.observableArrayList();

    @Autowired IbuttonService ibuttonService;

    private static Logger logger = LoggerFactory.getLogger(ConnectedDevicesController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        serialColumn.setCellValueFactory(cellData -> cellData.getValue().serialProperty());
        aliasColumn.setCellValueFactory(cellData -> cellData.getValue().aliasProperty());
        positionColumn.setCellValueFactory(cellData -> cellData.getValue().defaultPositionProperty());

        connectedDevicesTable.setItems(devicesConnected);
        connectedDevicesTable.getColumns().addAll(modelColumn, serialColumn, aliasColumn, positionColumn);
        connectedDevicesTable.setPlaceholder(new Label(""));    // no placeholder

        connectedDevicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, device) -> {
            infoTabPane.getSelectionModel().clearSelection();   // stop RealTime task if was selected
            if (device != null) {
                loadInfo(device);
            }
        });

        connectedDevicesTable.getSelectionModel().clearSelection();
        devicesConnected.addListener((ListChangeListener<Device>) c -> {
            updateIndicator();
        });

        updateIndicator();
    }

    /**
     * If there is no device connected, show the progressIndicator to let the user know that the application is searching
     */
    private void updateIndicator() {
        if (devicesConnected.size() == 0) {
            searchingIndicator.setVisible(true);
        } else {
            searchingIndicator.setVisible(false);
        }
    }

    /**
     * Check the type of device selected and the container that implements. Load the tabs that correspond to this device.
     * @param device device to show info of
     */
    private void loadInfo(Device device) {
        infoTabPane.getTabs().clear();      // clear previous selection -> stop all tasks
        Animation.fadeInTransition(infoTabPane);

        infoTabPane.getTabs().add(generalTab(device));

        if (device.getContainer() != null && temperatureContainerSupported(device.getContainer())) {
            infoTabPane.getTabs().add(currentTemperatureTab(device));
        }

        if(device.getContainer() != null && missionContainerSupported(device.getContainer())) {
            infoTabPane.getTabs().add(deviceMissionTab(device));
        }
    }

    private Tab generalTab(Device device) {

        return new Tab();
    }

    private Tab deviceMissionTab(Device device) {
        Tab missionInfoTab = new Tab();
        StackPane stackPane = new StackPane();

        // Load a new pane for the tab
        Node missionInfoPane = VistaNavigator.loader.load(ConnectedDevicesController.class.getResource(Constants.DEVICE_MISSION_INFO));
        DeviceMissionInformationController deviceMissionInformationController = VistaNavigator.loader.getController();
        deviceMissionInformationController.setDevice(device);

        stackPane.getChildren().setAll(missionInfoPane);
        missionInfoTab.setContent(stackPane);
        missionInfoTab.setText("Mission Info");

        missionInfoTab.setOnSelectionChanged(t -> {

        });

        return missionInfoTab;
    }

    private Tab currentTemperatureTab(Device device) {
        Tab realTimeTempTab = new Tab();
        StackPane stackPane = new StackPane();

        // Load a new pane for the tab
        Node realTimeTempPane = VistaNavigator.loader.load(ConnectedDevicesController.class.getResource(Constants.REAL_TIME_TEMP));
        RealTimeTemperatureController realTimeTemperatureController = VistaNavigator.loader.getController();
        realTimeTemperatureController.setDevice(device);

        stackPane.getChildren().setAll(realTimeTempPane);
        realTimeTempTab.setContent(stackPane);
        realTimeTempTab.setText("Real Time Temperature");

        realTimeTempTab.setOnSelectionChanged(t -> {
            if (realTimeTempTab.isSelected()) {
                realTimeTemperatureController.startReading();
            } else {
                realTimeTemperatureController.stopReading();
            }
        });

        return realTimeTempTab;
    }

    @Override
    public void arrival(DeviceDetector event) {
        OneWireContainer container = event.getContainer();
        String adapterName = event.getAdapterName();
        String adapterPort = event.getAdapterPort();

        String model = container.getName();
        String serial = container.getAddressAsString();
        String alternateNames = container.getAlternateNames();

        Device device = new Device();
        device.setContainer(container);
        device.setModel(model);
        device.setSerial(serial);
        device.setAdapterName(adapterName);
        device.setAdapterPort(adapterPort);

        Platform.runLater(() -> {
            addDeviceToTable(device);
        });
    }

    private void addDeviceToTable(Device device) {

        Ibutton ibutton = ibuttonService.getBySerial(device.getSerial());

        if (ibutton != null) {
            device.setAlias(ibutton.getAlias());

            if (ibutton.getPosition() != null) {
                device.setDefaultPosition(ibutton.getPosition().getPlace());
            }
        }

        devicesConnected.add(device);
    }

    private void removeDeviceFromTable(String serial) {
        for (Device device : devicesConnected) {
            if (serial.equals(device.getSerial())) {
                devicesConnected.remove(device);
                try {
                    connectedDevicesTable.getSelectionModel().clearSelection();
                    Animation.fadeOutTransition(infoTabPane);
                } catch (Exception e) {
                    logger.warn("Exception thrown while removing device from table: " + e.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void departure(DeviceDetector event) {
        String serial = event.getSerial();

        Platform.runLater(() -> removeDeviceFromTable(serial));
    }

    @Override
    public void reload(Object object) {
        if (object instanceof Ibutton) {
            for (Device device : devicesConnected) {
                if (device.getSerial().equals(((Ibutton) object).getSerial())) {
                    device.setAlias(((Ibutton) object).getAlias());

                    if (((Ibutton) object).getPosition() != null) {
                        device.setDefaultPosition(((Ibutton) object).getPosition().getPlace());
                    }
                }
            }

        }
    }

    /**
     * Checks if this container supports the temperature view.
     *
     * @param owc - container to check for viewer support.
     * @return 'true' if this viewer supports the provided
     * container.
     */
    private boolean temperatureContainerSupported(OneWireContainer owc) {
        return (owc instanceof TemperatureContainer);
    }

    /**
     * Checks if this container supports the mission view.
     *
     * @param owc - container to check for viewer support.
     * @return 'true' if this viewer supports the provided
     * container.
     */
    private boolean missionContainerSupported(OneWireContainer owc) {
        return (owc instanceof MissionContainer);
    }

    @Override
    public void translate() {
        modelColumn.setText("  Model");
        serialColumn.setText("  Serial");
        aliasColumn.setText("  Alias");
        positionColumn.setText("  Default Position");
    }

}
