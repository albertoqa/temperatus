package temperatus.controller.button;

import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import temperatus.controller.creation.RecordConfigController;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionDisableTask;
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
 * Created by alberto on 23/1/16.
 */
@Controller
public class ConnectedDevicesController implements Initializable, AbstractController, DeviceDetectorListener {

    @FXML private Button newMissionButton;
    @FXML private Button configureButton;

    @FXML private TabPane infoTabPane;
    @FXML private TableView<Device> connectedDevicesTable;

    @FXML private HBox searchingIndicator;

    private TableColumn<Device, String> modelColumn = new TableColumn<>("  Model");
    private TableColumn<Device, String> serialColumn = new TableColumn<>("  Serial");
    private TableColumn<Device, String> aliasColumn = new TableColumn<>("  Alias");
    private TableColumn<Device, String> positionColumn = new TableColumn<>("  Default Position");

    private ObservableList<Device> devicesConnected = FXCollections.observableArrayList();

    @Autowired DeviceMissionDisableTask deviceMissionDisableTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;

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

        connectedDevicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            infoTabPane.getSelectionModel().clearSelection();
            if (newSelection != null) {
                loadInfo(newSelection);
            }
        });

        connectedDevicesTable.getSelectionModel().clearSelection();

        devicesConnected.addListener((ListChangeListener<Device>) c -> {
            updateIndicator();
        });

        updateIndicator();
    }

    private void updateIndicator() {
        if (devicesConnected.size() == 0) {
            searchingIndicator.setVisible(true);
        } else {
            searchingIndicator.setVisible(false);
        }
    }

    private void loadInfo(Device device) {
        infoTabPane.getTabs().clear();
        Animation.fadeInTransition(infoTabPane);

        infoTabPane.getTabs().add(generalTab(device));

        if (device.getContainer() != null && temperatureContainerSupported(device.getContainer())) {
            infoTabPane.getTabs().add(currentTemperatureTab(device));
        }
    }

    private Tab generalTab(Device device) {

        return new Tab();
    }

    private Tab currentTemperatureTab(Device device) {
        Tab realTimeTempTab = new Tab();
        StackPane stackPane = new StackPane();

        // Load a new pane for the tab
        Node realTimeTempPane = VistaNavigator.loader.load(RecordConfigController.class.getResource(Constants.REAL_TIME_TEMP));
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

    @FXML
    private void newMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
        VistaNavigator.baseController.selectMenuButton(Constants.NEW_MISSION);
    }

    @FXML
    private void configureIbutton() {
        VistaNavigator.loadVista(Constants.CONFIG_DEVICE);
    }

    @Override
    public void translate() {

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
                connectedDevicesTable.getSelectionModel().clearSelection();
                Animation.fadeOutTransition(infoTabPane);
                break;
            }
        }
    }

    @FXML
    private void stopDeviceMission() {

        Device device = connectedDevicesTable.getSelectionModel().getSelectedItem();

        if (missionContainerSupported(device.getContainer())) {

            deviceMissionDisableTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort());  // device connection data
            ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionDisableTask);

            // TODO show progress???

            Futures.addCallback(future, new FutureCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    Platform.runLater(() -> {
                        logger.info("Device configured correctly");
                    });
                }

                public void onFailure(Throwable thrown) {
                    logger.error("Error starting mission on device - Future error");
                }
            });
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
    public boolean missionContainerSupported(OneWireContainer owc) {
        return (owc instanceof MissionContainer);
    }

}
