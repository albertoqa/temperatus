package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 7/2/16.
 */
@Controller
@Scope("prototype")
public class RecordInfoPaneController implements Initializable, AbstractController {

    @FXML private Label model;
    @FXML private Label sampleRate;
    @FXML private Label startTime;
    @FXML private Label stopTime;
    @FXML private Label totalMeasurements;
    @FXML private Label maxTemperature;
    @FXML private Label minTemperature;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setData(String model, String sampleRate, String startTime, String stopTime, String totalMeasurements, String maxTemp, String minTemp) {
        /*this.model.setText(model);
        this.sampleRate.setText(sampleRate);
        this.startTime.setText(startTime);
        this.stopTime.setText(stopTime);
        this.totalMeasurements.setText(totalMeasurements);
        this.maxTemperature.setText(maxTemp);
        this.minTemperature.setText(minTemp);*/
    }

    @Override
    public void translate() {

    }
}
