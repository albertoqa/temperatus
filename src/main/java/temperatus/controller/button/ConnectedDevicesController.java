package temperatus.controller.button;

import com.dalsemi.onewire.container.OneWireContainer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.listener.DeviceDetector;
import temperatus.listener.DeviceDetectorListener;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.types.Device;
import temperatus.model.service.IbuttonService;
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

    private TableColumn<Device, String> modelColumn = new TableColumn<>("  Model");
    private TableColumn<Device, String> serialColumn = new TableColumn<>("  Serial");
    private TableColumn<Device, String> aliasColumn = new TableColumn<>("  Alias");
    private TableColumn<Device, String> positionColumn = new TableColumn<>("  Default Position");

    private ObservableList<Device> devicesConnected = FXCollections.observableArrayList();

    @Autowired IbuttonService ibuttonService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);

        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        serialColumn.setCellValueFactory(cellData -> cellData.getValue().serialProperty());
        aliasColumn.setCellValueFactory(cellData -> cellData.getValue().aliasProperty());
        positionColumn.setCellValueFactory(cellData -> cellData.getValue().defaultPositionProperty());

        connectedDevicesTable.setItems(devicesConnected);
        connectedDevicesTable.getColumns().addAll(modelColumn, serialColumn, aliasColumn, positionColumn);
        connectedDevicesTable.setPlaceholder(new Label("No iButtons Connected"));

        connectedDevicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            loadInfo(newSelection);
        });
    }

    private void loadInfo(Device device) {



    }

    private void search() {

    }

    private void addIbuttonToTable() {

    }

    @FXML
    private void newMission() {
        VistaNavigator.loadVista(Constants.NEW_MISSION);
    }

    @FXML
    private void configureIbutton() {

    }

    @Override
    public void translate() {

    }

    @Override
    public void arrival(DeviceDetector event) {
        OneWireContainer container = event.getContainer();

        String model = container.getName();
        String serial = container.getAddressAsString();
        String alternateNames = container.getAlternateNames();
        String alias = "";

        Device device = new Device();
        device.setContainer(container);
        device.setModel(model);
        device.setSerial(serial);

        Platform.runLater(new Runnable() {
            public void run() {
                addDeviceToTable(device);
            }
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
                break;
            }
        }
    }

    @Override
    public void departure(DeviceDetector event) {
        String serial = event.getSerial();

        Platform.runLater(new Runnable() {
            public void run() {
                removeDeviceFromTable(serial);
            }
        });
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
}
