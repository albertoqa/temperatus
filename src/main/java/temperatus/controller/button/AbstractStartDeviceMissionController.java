package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Configuration;
import temperatus.model.service.ConfigurationService;
import temperatus.util.Constants;
import temperatus.util.IntegerSpinner;
import temperatus.util.TextValidation;

import java.text.ParseException;
import java.util.Date;

import static temperatus.controller.AbstractController.language;

/**
 * Abstract class to keep all common data between StartDeviceMission and NewConfiguration controllers
 * <p>
 * Created by alberto on 23/4/16.
 */
public abstract class AbstractStartDeviceMissionController {

    @FXML private Label nameLabel;
    @FXML private Label rateLabel;
    @FXML private Label resolutionLabel;
    @FXML private Label startLabel;
    @FXML private Label highLabel;
    @FXML private Label lowLabel;
    @FXML private Label alarmLabel;
    @FXML private Label observationsLabel;

    @FXML private RadioButton immediatelyCheck;
    @FXML private RadioButton onDateCheck;
    @FXML private RadioButton onAlarmCheck;
    @FXML private RadioButton delayCheck;

    @FXML private CheckBox syncTime;
    @FXML private CheckBox rollOver;
    @FXML private CheckBox activateAlarmCheck;

    @FXML private Spinner<Double> highAlarm;
    @FXML private Spinner<Double> lowAlarm;
    @FXML private Spinner<Integer> delayInput;
    @FXML private Spinner<Integer> onAlarmDelayInput;

    @FXML private TextField nameInput;
    @FXML private TextField dateInput;
    @FXML private TextField rateInput;
    @FXML private TextArea observationsArea;

    @FXML private ChoiceBox<String> resolutionBox;

    @Autowired ConfigurationService configurationService;

    private ToggleGroup startGroup = new ToggleGroup();

    private static final String RESOLUTION_LOW = "0.5 (low)";
    private static final String RESOLUTION_HIGH = "0.065 (high)";
    private static final double RES_LOW = 0.5;
    private static final double RES_HIGH = 0.065;

    private static final int MAX_NUMBER_FOR_RATE_INPUT = 20;
    private static final String EMPTY = "";

    /**
     * Initialize all the elements of the view
     */
    void initializeViewElements() {
        startGroup.getToggles().addAll(immediatelyCheck, onDateCheck, onAlarmCheck, delayCheck);
        immediatelyCheck.setSelected(true);

        onDateCheck.selectedProperty().addListener((observable, oldValue, newValue) -> dateInput.setVisible(newValue));
        onAlarmCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onAlarmDelayInput.setVisible(newValue));
        delayCheck.selectedProperty().addListener((observable, oldValue, newValue) -> delayInput.setVisible(newValue));

        IntegerSpinner.setSpinner(delayInput);
        IntegerSpinner.setSpinner(onAlarmDelayInput);

        activateAlarmCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highAlarm.setDisable(false);
                lowAlarm.setDisable(false);
            } else {
                highAlarm.setDisable(true);
                highAlarm.getEditor().setText(EMPTY);
                lowAlarm.setDisable(true);
                lowAlarm.getEditor().setText(EMPTY);
            }
        });

        rateInput.addEventFilter(KeyEvent.KEY_TYPED, TextValidation.numeric(MAX_NUMBER_FOR_RATE_INPUT));

        resolutionBox.getItems().addAll(RESOLUTION_LOW, RESOLUTION_HIGH);
        resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
    }

    /**
     * Generate a configuration object with the values obtained from the user input
     */
    void generateConfiguration(Configuration configuration) throws ControlledTemperatusException {
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

        syncTime.setSelected(configuration.isSyncTime());
        rollOver.setSelected(configuration.isRollover());
        onAlarmCheck.setSelected(configuration.isSuta());

        delayInput.getEditor().setText(String.valueOf(configuration.getDelay()));
        onAlarmDelayInput.getEditor().setText(String.valueOf(configuration.getDelay()));

        if (configuration.getResolutionC1() == RES_LOW) {
            resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
        } else {
            resolutionBox.getSelectionModel().select(RESOLUTION_HIGH);
        }

        if (configuration.getEnableAlarmC1()) {
            activateAlarmCheck.setSelected(true);
            try {
                highAlarm.getEditor().setText(String.valueOf(configuration.getHighAlarmC1()));
                lowAlarm.getEditor().setText(String.valueOf(configuration.getLowAlarmC1()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            activateAlarmCheck.setSelected(false);
        }
    }

    /**
     * Get the start delay set by the user depending of the type of start
     *
     * @return start delay in seconds
     */
    private int getStart() throws ControlledTemperatusException {
        if (immediatelyCheck.isSelected()) {
            return 0;
        } else if (delayCheck.isSelected()) {
            return delayInput.getValue();
        } else if (onDateCheck.isSelected()) {
            return calculateDateDelay(dateInput.getText());
        } else {
            return onAlarmDelayInput.getValue();
        }
    }

    /**
     * Calculate the number of seconds between now and the time set by the user
     *
     * @param d time set by the user
     * @return number of seconds in between
     */
    private int calculateDateDelay(String d) throws ControlledTemperatusException {
        try {
            return (int) (Constants.dateTimeFormat.parse(d).getTime() - new Date().getTime()) / 1000;
        } catch (ParseException e) {
            throw new ControlledTemperatusException(language.get(Lang.ERROR_PARSE_DATE));
        }
    }

    /**
     * Translate the common elements
     */
    void translateCommon() {
        nameInput.setPromptText(language.get(Lang.NAMEPROMPT));
        nameLabel.setText(language.get(Lang.NAMELABEL));

        rateLabel.setText(language.get(Lang.RATE_LABEL));
        resolutionLabel.setText(language.get(Lang.RESOLUTION_LABEL));
        startLabel.setText(language.get(Lang.STARTDATELABEL));
        highLabel.setText(language.get(Lang.HIGH_ALARM_LABEL));
        lowLabel.setText(language.get(Lang.LOW_ALARM_LABEL));
        alarmLabel.setText(language.get(Lang.SET_ALARM_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONSLABEL));

        immediatelyCheck.setText(language.get(Lang.IMMEDIATELY));
        onDateCheck.setText(language.get(Lang.ON_DATE));
        onAlarmCheck.setText(language.get(Lang.ON_ALARM));
        delayCheck.setText(language.get(Lang.ON_DELAY));

        syncTime.setText(language.get(Lang.SYNC_CHECK));
        rollOver.setText(language.get(Lang.ROLL_OVER_CHECK));
        activateAlarmCheck.setText(language.get(Lang.SET_ALARM_CHECK));

        rateInput.setPromptText(language.get(Lang.RATE_PROMPT));
        observationsArea.setPromptText(language.get(Lang.OBSERVATIONSPROMPT));
    }
}
