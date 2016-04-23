package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import temperatus.model.pojo.Configuration;
import temperatus.model.service.ConfigurationService;
import temperatus.util.Constants;

import java.text.ParseException;
import java.util.Date;

/**
 * Abstract class to keep all common data between StartDeviceMission and NewConfiguration controllers
 * <p>
 * Created by alberto on 23/4/16.
 */
public abstract class AbstractStartDeviceMissionController {

    @FXML Label nameLabel;
    @FXML Label rateLabel;
    @FXML Label resolutionLabel;
    @FXML Label startLabel;
    @FXML Label highLabel;
    @FXML Label lowLabel;
    @FXML Label alarmLabel;
    @FXML Label observationsLabel;

    @FXML RadioButton immediatelyCheck;
    @FXML RadioButton onDateCheck;
    @FXML RadioButton onAlarmCheck;
    @FXML RadioButton delayCheck;

    @FXML CheckBox syncTime;
    @FXML CheckBox rollOver;
    @FXML CheckBox activateAlarmCheck;

    @FXML Spinner<Double> highAlarm;
    @FXML Spinner<Double> lowAlarm;
    @FXML Spinner<Integer> delayInput;
    @FXML Spinner<Integer> onAlarmDelayInput;

    @FXML TextField nameInput;
    @FXML TextField dateInput;
    @FXML TextField rateInput;
    @FXML TextArea observationsArea;

    @FXML ChoiceBox<String> resolutionBox;

    @Autowired ConfigurationService configurationService;

    ToggleGroup startGroup = new ToggleGroup();

    static final String RESOLUTION_LOW = "0.5 (low)";
    static final String RESOLUTION_HIGH = "0.065 (high)";
    private static final double RES_LOW = 0.5;
    private static final double RES_HIGH = 0.065;

    /**
     * Depending on the type of start, the user will be required to input different values
     */
    void addListenersToStartTypes() {
        onDateCheck.selectedProperty().addListener((observable, oldValue, newValue) -> dateInput.setVisible(newValue));
        onAlarmCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onAlarmDelayInput.setVisible(newValue));
        delayCheck.selectedProperty().addListener((observable, oldValue, newValue) -> delayInput.setVisible(newValue));
    }

    /**
     * Generate a configuration object with the values obtained from the user input
     */
    void generateConfiguration(Configuration configuration) {
        // TODO throw exception is something wrong
        configuration.setName(nameInput.getText());
        configuration.setSyncTime(syncTime.isSelected());
        configuration.setRollover(rollOver.isSelected());
        configuration.setDelay(getStart());
        configuration.setRate(Integer.valueOf(rateInput.getText()));
        configuration.setSuta(onAlarmCheck.isSelected());

        configuration.setChannelEnabledC1(true);
        configuration.setChannelEnabledC2(false);
        configuration.setResolutionC1(resolutionBox.getSelectionModel().getSelectedItem().equals(RESOLUTION_LOW) ? RES_LOW : RES_HIGH);
        if (activateAlarmCheck.isSelected()) {
            configuration.setHighAlarmC1(highAlarm.getValue());
            configuration.setLowAlarmC1(lowAlarm.getValue());
            configuration.setEnableAlarmC1(true);
        } else {
            configuration.setEnableAlarmC1(false);
        }

        configuration.setObservations(observationsArea.getText());
    }

    /**
     * Load configuration data on the view
     *
     * @param configuration to be loaded
     */
    void loadConfiguration(Configuration configuration) {
        nameInput.setText(configuration.getName());
        rateInput.setText(String.valueOf(configuration.getRate()));
        observationsArea.setText(configuration.getObservations());

        if (configuration.isSyncTime()) {
            syncTime.setSelected(true);
        } else {
            syncTime.setSelected(false);
        }

        if (configuration.isRollover()) {
            rollOver.setSelected(true);
        } else {
            rollOver.setSelected(false);
        }

        if (configuration.isSuta()) {
            onAlarmCheck.setSelected(true);
        } else {
            onAlarmCheck.setSelected(false);
        }

        delayInput.getEditor().setText(String.valueOf(configuration.getDelay()));
        onAlarmDelayInput.getEditor().setText(String.valueOf(configuration.getDelay()));

        if (configuration.getResolutionC1() == RES_LOW) {
            resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
        } else {
            resolutionBox.getSelectionModel().select(RESOLUTION_HIGH);
        }

        if (configuration.getEnableAlarmC1()) {
            activateAlarmCheck.setSelected(true);
            highAlarm.getEditor().setText(String.valueOf(configuration.getHighAlarmC1()));
            lowAlarm.getEditor().setText(String.valueOf(configuration.getLowAlarmC1()));
        } else {
            activateAlarmCheck.setSelected(false);
        }
    }

    private int getStart() {
        if (immediatelyCheck.isSelected()) {
            return 0;
        } else if (delayCheck.isSelected()) {
            return Integer.valueOf(delayInput.getEditor().getText());
        } else if (onDateCheck.isSelected()) {
            return calculateDateDelay(dateInput.getText());
        } else {
            return onAlarmDelayInput.getValue();
        }
    }

    private int calculateDateDelay(String d) {
        try {
            return (int) (Constants.dateTimeFormat.parse(d).getTime() - new Date().getTime()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0; //TODO
    }
}
