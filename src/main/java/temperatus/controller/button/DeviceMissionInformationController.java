package temperatus.controller.button;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceMissionDisableTask;
import temperatus.model.pojo.types.Device;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 24/4/16.
 */
@Controller
public class DeviceMissionInformationController implements Initializable, AbstractController {

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

    @Autowired DeviceMissionDisableTask deviceMissionDisableTask;   // read from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private Device device;
    private static Logger logger = LoggerFactory.getLogger(DeviceMissionInformationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

    }

    /**
     * Set the selected device to read mission info from
     *
     * @param device device to read from
     */
    void setDevice(Device device) {
        this.device = device;
        // TODO show progress indicator
        // TODO read task
    }

    /**
     * Open a modal window showing all measurements read by the device in a graphic
     */
    @FXML
    private void showTemperatureData() {

    }

    /**
     * Open the view to configure a new mission on the device
     */
    @FXML
    private void configureIbutton() {
        StartDeviceMissionController startDeviceMissionController = VistaNavigator.loadVista(Constants.CONFIG_DEVICE);
        assert startDeviceMissionController != null;
        startDeviceMissionController.selectDevice(device);
    }

    /**
     * Stop a mission on the device (if currently running)
     */
    @FXML
    private void stopDeviceMission() {
        if (device != null) {
            startProgressIndicator();

            deviceMissionDisableTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort());  // device connection data
            ListenableFuture future = deviceOperationsManager.submitTask(deviceMissionDisableTask);

            Futures.addCallback(future, new FutureCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    Platform.runLater(() -> {
                        stopProgressIndicator();
                        logger.info("Device configured correctly");});
                }

                public void onFailure(Throwable thrown) {
                    stopProgressIndicator();
                    // TODO show error
                    logger.error("Error starting mission on device - Future error");
                }
            });
        }
    }

    /**
     * Start the progress indicator and blur the pane
     */
    private void startProgressIndicator() {
        anchorPane.setDisable(true);    // blur pane
        VBox box = new VBox(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS)); // add a progress indicator to the view
        box.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(box);
    }

    /**
     * End the progress indicator and activate the anchor pane
     */
    private void stopProgressIndicator() {
        stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
        anchorPane.setDisable(false);
    }

    @Override
    public void translate() {

    }
}
