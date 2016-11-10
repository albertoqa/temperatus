package temperatus.controller.button;

import com.dalsemi.onewire.container.MissionContainer;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceConnectedList;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionDisableTask;
import temperatus.lang.Lang;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.types.Device;
import temperatus.model.service.IbuttonService;
import temperatus.util.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Show all the devices connected to the computer and their information. Depending on
 * the type of device the information shown will be direfert.
 * <p>
 * Created by alberto on 23/1/16.
 */
@Controller
public class ConnectedDevicesController implements Initializable, AbstractController {

    @FXML private StackPane stackPane;
    @FXML private Button disableAllButton;

    @FXML private Label headerLabel;
    @FXML private Label searchingLabel;

    @FXML private TabPane infoTabPane;
    @FXML private TableView<Device> connectedDevicesTable;

    @FXML private ProgressIndicator progressIndicator;
    @FXML private HBox searchingIndicator;

    @FXML private Button configureButton;

    private TableColumn<Device, String> modelColumn = new TableColumn<>();
    private TableColumn<Device, String> serialColumn = new TableColumn<>();
    private TableColumn<Device, String> aliasColumn = new TableColumn<>();
    private TableColumn<Device, String> positionColumn = new TableColumn<>();

    @Autowired IbuttonService ibuttonService;
    @Autowired DeviceConnectedList deviceConnectedList;
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private static Logger logger = LoggerFactory.getLogger(ConnectedDevicesController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        serialColumn.setCellValueFactory(cellData -> cellData.getValue().serialProperty());
        aliasColumn.setCellValueFactory(cellData -> cellData.getValue().aliasProperty());
        positionColumn.setCellValueFactory(cellData -> cellData.getValue().defaultPositionProperty());

        aliasColumn.setComparator(new NaturalOrderComparator());

        connectedDevicesTable.setItems(deviceConnectedList.getDevices());
        connectedDevicesTable.getColumns().addAll(modelColumn, serialColumn, aliasColumn, positionColumn);
        connectedDevicesTable.setPlaceholder(new Label(Constants.EMPTY));    // no placeholder

        connectedDevicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, device) -> {
            infoTabPane.getSelectionModel().clearSelection();   // stop RealTime task if was selected
            if (device != null) {
                loadInfo(device);
            }
        });

        connectedDevicesTable.getSelectionModel().clearSelection();
        deviceConnectedList.getDevices().addListener((ListChangeListener<? super Device>) c -> updateIndicator());

        updateIndicator();
    }

    /**
     * If there is no device connected, show the progressIndicator to let the user know that the application is searching
     */
    private void updateIndicator() {
        if (deviceConnectedList.getDevices().size() == 0) {
            searchingIndicator.setVisible(true);
            configureButton.setDisable(true);
            disableAllButton.setDisable(true);
            Animation.fadeOutTransition(infoTabPane);
            infoTabPane.getTabs().clear();      // clear previous selection -> stop all tasks
        } else {
            searchingIndicator.setVisible(false);
            configureButton.setDisable(false);
            disableAllButton.setDisable(false);
        }
        logger.debug("No devices... showing search indicator");
    }

    /**
     * Check the type of device selected and the container that implements. Load the tabs that correspond to this device.
     *
     * @param device device to show info of
     */
    private void loadInfo(Device device) {
        infoTabPane.getTabs().clear();      // clear previous selection -> stop all tasks
        Animation.fadeInTransition(infoTabPane);

        infoTabPane.getTabs().add(generalTab(device));  // every device has a general tab

        // if device can read temperature show this view
        if (device.getContainer() != null && temperatureContainerSupported(device.getContainer())) {
            infoTabPane.getTabs().add(currentTemperatureTab(device));
        }

        // if device can configure missions show this view
        if (device.getContainer() != null && missionContainerSupported(device.getContainer())) {
            infoTabPane.getTabs().add(deviceMissionTab(device));
        }
    }

    /**
     * Open the view to configure a new mission on the device
     */
    @FXML
    private void configureIbutton() {
        VistaNavigator.loadVista(Constants.CONFIG_DEVICE);
    }

    /**
     * Load the general information of the device
     *
     * @param device device to show
     * @return tab with the general information
     */
    private Tab generalTab(Device device) {
        Tab generalTab = new Tab();
        StackPane stackPane = new StackPane();

        // Load a new pane for the tab
        Node generalInfoPane = VistaNavigator.loader.load(ConnectedDevicesController.class.getResource(Constants.DEVICE_GENERAL_INFO));
        DeviceGeneralInfoController deviceGeneralInfoController = VistaNavigator.loader.getController();
        deviceGeneralInfoController.setDevice(device);

        stackPane.getChildren().setAll(generalInfoPane);
        generalTab.setContent(stackPane);
        generalTab.setText(language.get(Lang.GENERAL_TAB));

        return generalTab;
    }

    /**
     * Load a tab to check the missions of the device
     *
     * @param device device to read from
     * @return tab with the mission information
     */
    private Tab deviceMissionTab(Device device) {
        Tab missionInfoTab = new Tab();
        StackPane stackPane = new StackPane();

        // Load a new pane for the tab
        Node missionInfoPane = VistaNavigator.loader.load(ConnectedDevicesController.class.getResource(Constants.DEVICE_MISSION_INFO));
        DeviceMissionInformationController deviceMissionInformationController = VistaNavigator.loader.getController();
        deviceMissionInformationController.setDevice(device);

        stackPane.getChildren().setAll(missionInfoPane);
        missionInfoTab.setContent(stackPane);
        missionInfoTab.setText(language.get(Lang.MISSION_INFO));

        return missionInfoTab;
    }

    /**
     * Load a tab to check the temperature in real time from the device selected
     *
     * @param device device to read from
     * @return tab with the temperature information
     */
    private Tab currentTemperatureTab(Device device) {
        Tab realTimeTempTab = new Tab();
        StackPane stackPane = new StackPane();

        // Load a new pane for the tab
        Node realTimeTempPane = VistaNavigator.loader.load(ConnectedDevicesController.class.getResource(Constants.REAL_TIME_TEMP));
        RealTimeTemperatureController realTimeTemperatureController = VistaNavigator.loader.getController();
        realTimeTemperatureController.setDevice(device);

        stackPane.getChildren().setAll(realTimeTempPane);
        realTimeTempTab.setContent(stackPane);
        realTimeTempTab.setText(language.get(Lang.REAL_TIME_TEMP));

        /**
         * Stop the task of read temperature if tab is not selected and start it when selected
         */
        realTimeTempTab.setOnSelectionChanged(t -> {
            if (realTimeTempTab.isSelected()) {
                realTimeTemperatureController.startReading();
            } else {
                realTimeTemperatureController.stopReading();
            }
        });

        return realTimeTempTab;
    }

    /**
     * Disable all missions actives in the connected devices
     */
    @FXML
    private void disableAllMissions() {
        AtomicInteger started = new AtomicInteger(0);
        AtomicInteger finished = new AtomicInteger(0);

        deviceConnectedList.getDevices().stream().filter(device -> missionContainerSupported(device.getContainer())).forEach(device -> {
            startProgressIndicator();

            DeviceMissionDisableTask deviceMissionDisableTask = new DeviceMissionDisableTask();   // read from device task
            deviceMissionDisableTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), false);  // device connection data
            ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionDisableTask);
            started.set(started.get()+1);

            history.info(User.getUserName() + Constants.SPACE + language.get(Lang.STOP_MISSION_HISTORY) + Constants.SPACE + device.getAlias());

            Futures.addCallback(future, new FutureCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    finished.set(finished.get()+1);
                    if(started.get() == finished.get()) {
                        Platform.runLater(() -> stopProgressIndicator());
                    }
                }

                public void onFailure(Throwable thrown) {
                    Platform.runLater(() -> {
                        stopProgressIndicator();
                        showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_STOPPING_MISSION));
                        logger.error("Error stopping mission on device - Future error");
                    });
                }
            });

        });
    }

    /**
     * Start the progress indicator and blur the pane
     */
    private void startProgressIndicator() {
        if(!stackPane.isDisable()) {
            stackPane.setDisable(true);    // blur pane
            VBox box = new VBox(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS)); // add a progress indicator to the view
            box.setAlignment(Pos.CENTER);
            stackPane.getChildren().add(box);
        }
    }

    /**
     * End the progress indicator and activate the anchor pane
     */
    private void stopProgressIndicator() {
        if (stackPane.getChildren().size() > 1) {
            stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
        }
        stackPane.setDisable(false);
    }

    /**
     * If new device registered to database, reload its alias and default position to show
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Ibutton) {
            for (Device device : deviceConnectedList.getDevices()) {
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
        logger.debug("Checking if temperatureContainer supported");
        return owc instanceof TemperatureContainer;
    }

    /**
     * Checks if this container supports the mission view.
     *
     * @param owc - container to check for viewer support.
     * @return 'true' if this viewer supports the provided
     * container.
     */
    private boolean missionContainerSupported(OneWireContainer owc) {
        logger.debug("Checking if missionContainer supported");
        return owc instanceof MissionContainer;
    }

    @Override
    public void translate() {
        modelColumn.setText(language.get(Lang.MODEL_COLUMN));
        serialColumn.setText(language.get(Lang.SERIAL_COLUMN));
        aliasColumn.setText(language.get(Lang.ALIAS_COLUMN));
        positionColumn.setText(language.get(Lang.DEFAULT_POS_COLUMN));
        searchingLabel.setText(language.get(Lang.SEARCHING));
        headerLabel.setText(language.get(Lang.CONNECTED_DEVICES));
        configureButton.setText(language.get(Lang.CONFIGURE));
        disableAllButton.setText(language.get(Lang.DISABLE_ALL));
    }

}
