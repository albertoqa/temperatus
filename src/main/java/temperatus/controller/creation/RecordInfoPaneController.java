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

    @FXML private Label modelLabel;
    @FXML private Label serialLabel;
    @FXML private Label aliasLabel;
    @FXML private Label sampleRateLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label totalMeasurementsLabel;
    @FXML private Label positionLabel;

    @FXML private Label model;
    @FXML private Label serial;
    @FXML private Label alias;
    @FXML private Label sampleRate;
    @FXML private Label startDate;
    @FXML private Label endDate;
    @FXML private Label totalMeasurements;
    @FXML private Label position;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setData(String model, String serial, String alias, String sampleRate, String startTime, String endTime, String totalMeasurements, String position) {
        this.model.setText(model);
        this.serial.setText(serial);
        this.alias.setText(alias);
        this.sampleRate.setText(sampleRate);
        this.startDate.setText(startTime);
        this.endDate.setText(endTime);
        this.totalMeasurements.setText(totalMeasurements);
        this.position.setText(position);
    }

    @Override
    public void translate() {

    }
}
