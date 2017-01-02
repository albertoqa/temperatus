package temperatus.controller.device;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.calculator.Calculator;
import temperatus.controller.AbstractController;
import temperatus.device.DeviceOperationsManager;
import temperatus.device.task.DeviceRealTimeTempTask;
import temperatus.lang.Lang;
import temperatus.model.pojo.types.Device;
import temperatus.model.pojo.types.Unit;
import temperatus.model.pojo.utils.DateAxis;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Show a graphic with the temperature read form the device in real time
 * <p/>
 * Created by alberto on 18/4/16.
 */
@Controller
@Scope("prototype")
public class RealTimeTemperatureController implements Initializable, AbstractController {

    @FXML private LineChart<Date, Number> lineChart;
    @FXML private DateAxis dateAxis;            // time of measurement
    @FXML private NumberAxis temperatureAxis;   // temperature read

    @FXML private TextField currentTemp;    // show the current temperature in text
    @FXML private RadioButton unitC;
    @FXML private RadioButton unitF;

    private DeviceRealTimeTempTask deviceRealTimeTempTask = new DeviceRealTimeTempTask();   // read current temperature from device task
    @Autowired DeviceOperationsManager deviceOperationsManager;

    private Timeline readEvery;     // read temperature and show it in the graph every X seconds
    private XYChart.Series<Date, Number> serie = new XYChart.Series<>();
    private static final int period = 10;   // period of read device
    private ToggleGroup unitGroup = new ToggleGroup();

    private boolean newTemperature = true;     // only send a read task once the previous task has finished

    private static final String MISSION_ACTIVE = "Cant force temperature read during a mission";

    private static Logger logger = LoggerFactory.getLogger(RealTimeTemperatureController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
        newTemperature = true;

        unitGroup.getToggles().addAll(unitC, unitF);

        // Show the data using the preferred unit
        Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;
        if (Unit.C.equals(unit)) {
            unitGroup.selectToggle(unitC);
        } else {
            unitGroup.selectToggle(unitF);
        }

        unitGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(unitC)) {
                temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_C));
            } else {
                temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_F));
            }
        });

        createTimeLineWithPeriod();
        lineChart.setData(FXCollections.observableArrayList(serie));
    }

    /**
     * Set the selected device to read temperature from in the read task
     *
     * @param device device to read from
     */
    void setDevice(Device device) {
        deviceRealTimeTempTask.setDeviceData(device.getContainer(), device.getAdapterName(), device.getAdapterPort(), false);
    }

    /**
     * Call the readTemp task every PERIOD seconds until the user closes this tab
     */
    private void createTimeLineWithPeriod() {
        readEvery = new Timeline(new KeyFrame(Duration.seconds(period), event -> {
            if (newTemperature) {
                if (!(VistaNavigator.getController() instanceof ConnectedDevicesController)) {
                    readEvery.stop();
                    serie.getData().clear();
                }

                logger.info("Submit real time temperature read task");
                newTemperature = false;
                ListenableFuture future = deviceOperationsManager.submitTask(deviceRealTimeTempTask);
                Futures.addCallback(future, new FutureCallback<Double>() {
                    public void onSuccess(Double result) {
                        Platform.runLater(() -> addNewTemperature(result));
                    }

                    public void onFailure(Throwable thrown) {
                        Platform.runLater(() -> {
                            if (thrown.getMessage().contains(MISSION_ACTIVE)) {
                                currentTemp.setText(language.get(Lang.CANNOT_READ_TEMPERATURE_MISSION_ACTIVE));
                            } else {
                                currentTemp.setText(language.get(Lang.CANNOT_READ_TEMPERATURE_UNKNOWN_ERROR));
                            }
                            newTemperature = true;
                            logger.error("Error fetching temperature - Future error");
                        });
                    }
                });
            }
        }));

        readEvery.setCycleCount(Timeline.INDEFINITE);   // don't stop the task
    }

    /**
     * Add the new read temperature to the graph and to the textField
     *
     * @param temp temperature just read from the device
     */
    private void addNewTemperature(double temp) {
        newTemperature = true;
        double temperature = temp;

        if (unitGroup.getSelectedToggle().equals(unitF)) {
            temperature = Calculator.celsiusToFahrenheit(temperature);
        }

        serie.getData().add(new XYChart.Data<>(new Date(), temperature));
        currentTemp.setText(temperature + Constants.SPACE + (unitGroup.getSelectedToggle().equals(unitF) ? language.get(Lang.FAHRENHEIT) : language.get(Lang.CELSIUS)));
        logger.info("Temperature read: " + temperature);
    }

    /**
     * When the tab is selected it will start to read the device
     */
    void startReading() {
        readEvery.play();
    }

    /**
     * When the tab is deselected it will stop reading and it will clear the previously read data
     */
    void stopReading() {
        serie.getData().clear();
        readEvery.stop();
    }

    @Override
    public void translate() {
        currentTemp.setText(language.get(Lang.READING));
        serie.setName(language.get(Lang.REAL_TIME_TEMP));
        dateAxis.setLabel(language.get(Lang.DATE_AXIS));
        temperatureAxis.setLabel(language.get(Lang.TEMPERATURE_AXIS_C));
    }

}
