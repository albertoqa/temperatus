package temperatus.controller.button;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import temperatus.calculator.Calculator;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Configuration;
import temperatus.model.pojo.types.Unit;
import temperatus.model.service.ConfigurationService;
import temperatus.util.Constants;
import temperatus.util.DateUtils;
import temperatus.util.SpinnerFactory;
import temperatus.util.TextValidation;

import java.text.ParseException;
import java.time.LocalDateTime;
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
    @FXML public Spinner<Integer> delayInput;
    @FXML public Spinner<Integer> onAlarmDelayInput;

    @FXML public TextField nameInput;
    @FXML public TextField dateInput;
    @FXML public TextField rateInput;
    @FXML private TextArea observationsArea;

    @FXML public ChoiceBox<String> resolutionBox;

    @Autowired ConfigurationService configurationService;

    ToggleGroup startGroup = new ToggleGroup();

    private static final String RESOLUTION_LOW = "0.5 (low)";
    private static final String RESOLUTION_HIGH = "0.0625 (high)";
    private static final double RES_LOW = 0.5;
    private static final double RES_HIGH = 0.0625;

    private static final int START_DELAY = 1;
    private static final int START_DATE = 2;
    private static final int START_ALARM = 3;
    private static final int START_NOW = 4;

    private static final int MAX_NUMBER_FOR_RATE_INPUT = 20;

    private static Logger logger = LoggerFactory.getLogger(AbstractStartDeviceMissionController.class.getName());

    /**
     * Initialize all the elements of the view
     */
    void initializeViewElements() {
        startGroup.getToggles().addAll(immediatelyCheck, onDateCheck, onAlarmCheck, delayCheck);
        immediatelyCheck.setSelected(true);

        onDateCheck.selectedProperty().addListener((observable, oldValue, newValue) -> dateInput.setVisible(newValue));
        onAlarmCheck.selectedProperty().addListener((observable, oldValue, newValue) -> onAlarmDelayInput.setVisible(newValue));
        delayCheck.selectedProperty().addListener((observable, oldValue, newValue) -> delayInput.setVisible(newValue));

        SpinnerFactory.setIntegerSpinner(delayInput);
        SpinnerFactory.setIntegerSpinner(onAlarmDelayInput);
        delayInput.addEventFilter(KeyEvent.KEY_TYPED, SpinnerFactory.numeric());
        onAlarmDelayInput.addEventFilter(KeyEvent.KEY_TYPED, SpinnerFactory.numeric());

        activateAlarmCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                highAlarm.setDisable(false);
                lowAlarm.setDisable(false);
            } else {
                highAlarm.setDisable(true);
                highAlarm.getEditor().setText(Constants.EMPTY);
                lowAlarm.setDisable(true);
                lowAlarm.getEditor().setText(Constants.EMPTY);
            }
        });

        activateAlarmCheck.setSelected(false);

        SpinnerFactory.setDoubleSpinner(highAlarm);
        SpinnerFactory.setDoubleSpinner(lowAlarm);

        rateInput.addEventFilter(KeyEvent.KEY_TYPED, TextValidation.numeric(MAX_NUMBER_FOR_RATE_INPUT));

        resolutionBox.getItems().addAll(RESOLUTION_LOW, RESOLUTION_HIGH);
        resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
        dateInput.setText(Constants.dateTimeFormat.format(DateUtils.asUtilDate(LocalDateTime.now().plusMinutes(10))));

        syncTime.setSelected(true);
    }

    /**
     * Generate a configuration object with the values obtained from the user input
     */
    void generateConfiguration(Configuration configuration) throws ControlledTemperatusException {
        try {
            configuration.setName(nameInput.getText());
            configuration.setSyncTime(syncTime.isSelected());
            configuration.setRollover(rollOver.isSelected());
            configuration.setDelay(getStart());
            configuration.setRate(Integer.valueOf(rateInput.getText()));
            configuration.setSuta(onAlarmCheck.isSelected());

            if (delayCheck.isSelected()) {
                configuration.setStartN(START_DELAY);
            } else if (onDateCheck.isSelected()) {
                configuration.setStartN(START_DATE);
            } else if (onAlarmCheck.isSelected()) {
                configuration.setStartN(START_ALARM);
            } else {
                configuration.setStartN(START_NOW);
            }

            configuration.setChannelEnabledC1(true);
            configuration.setChannelEnabledC2(false);
            configuration.setResolutionC1(resolutionBox.getSelectionModel().getSelectedItem().equals(RESOLUTION_LOW) ? RES_LOW : RES_HIGH);
            if (activateAlarmCheck.isSelected()) {

                // Show the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

                if (Unit.C.equals(unit)) {
                    configuration.setHighAlarmC1(Double.valueOf(highAlarm.getEditor().getText().replace(Constants.COMMA, Constants.DOT)));
                    configuration.setLowAlarmC1(Double.valueOf(lowAlarm.getEditor().getText().replace(Constants.COMMA, Constants.DOT)));
                } else {
                    configuration.setHighAlarmC1(Calculator.fahrenheitToCelsius(Double.valueOf(highAlarm.getEditor().getText().replace(Constants.COMMA, Constants.DOT))));
                    configuration.setLowAlarmC1(Calculator.fahrenheitToCelsius(Double.valueOf(lowAlarm.getEditor().getText().replace(Constants.COMMA, Constants.DOT))));
                }
                configuration.setEnableAlarmC1(true);
            } else {
                configuration.setEnableAlarmC1(false);
            }

            configuration.setObservations(observationsArea.getText());
        } catch (ControlledTemperatusException ex) {
            logger.error("ControlledTemperatusException in line 167 of AbstractStartDeviceMissionController" + ex.getMessage());
            throw ex;
        } catch (Exception e) {
            logger.error("Invalid input number in AbstractStartDeviceMissionController");
            throw new ControlledTemperatusException(language.get(Lang.INVALID_INPUT_NUMBER));
        }
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

        if (configuration.getStartN() == START_DELAY) {
            delayCheck.setSelected(true);
            delayInput.getEditor().setText(String.valueOf(configuration.getDelay()));
        } else if (configuration.getStartN() == START_DATE) {
            onDateCheck.setSelected(true);
            dateInput.setText(Constants.dateTimeFormat.format(DateUtils.asUtilDate(LocalDateTime.now().plusMinutes(10))));
        } else if (configuration.getStartN() == START_ALARM) {
            onAlarmCheck.setSelected(true);
            onAlarmDelayInput.getEditor().setText(String.valueOf(configuration.getDelay()));
        }

        if (configuration.getResolutionC1() == RES_LOW) {
            resolutionBox.getSelectionModel().select(RESOLUTION_LOW);
        } else {
            resolutionBox.getSelectionModel().select(RESOLUTION_HIGH);
        }

        if (configuration.getEnableAlarmC1()) {
            activateAlarmCheck.setSelected(true);
            try {
                // Show the data using the preferred unit
                Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

                if (Unit.C.equals(unit)) {
                    highAlarm.getEditor().setText(String.valueOf(configuration.getHighAlarmC1()).replace(Constants.DOT, Constants.COMMA));
                    lowAlarm.getEditor().setText(String.valueOf(configuration.getLowAlarmC1()).replace(Constants.DOT, Constants.COMMA));
                } else {
                    highAlarm.getEditor().setText(String.valueOf(Calculator.celsiusToFahrenheit(configuration.getHighAlarmC1())).replace(Constants.DOT, Constants.COMMA));
                    lowAlarm.getEditor().setText(String.valueOf(Calculator.celsiusToFahrenheit(configuration.getLowAlarmC1())).replace(Constants.DOT, Constants.COMMA));
                }

            } catch (Exception ex) {
                logger.error("Error loading configuration: " + ex);
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
    int getStart() throws ControlledTemperatusException {
        int delay;
        if (immediatelyCheck.isSelected()) {
            delay = 0;
        } else if (delayCheck.isSelected()) {
            delay = Integer.valueOf(delayInput.getEditor().getText());
        } else if (onDateCheck.isSelected()) {
            delay = calculateDateDelay(dateInput.getText());
            logger.warn("Configured device for start at: " + dateInput.getText() + "\nThe current date is: " + new Date().getTime() + "\nThe calculated delay is: " + delay);
        } else {
            delay = Integer.valueOf(onAlarmDelayInput.getEditor().getText());
        }

        if (delay < 0) {
            throw new ControlledTemperatusException(language.get(Lang.INVALID_START_DELAY));
        }
        return delay;
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
            logger.error("Error parsing date: " + e);
            throw new ControlledTemperatusException(language.get(Lang.ERROR_PARSE_DATE));
        }
    }

    /**
     * Is the current selected resolution high?
     *
     * @return is high res selected?
     */
    boolean isResHigh() {
        return resolutionBox.getSelectionModel().getSelectedItem().equals(RESOLUTION_HIGH);
    }

    /**
     * Get the current date of start of the mission
     *
     * @return date of start
     * @throws ParseException
     */
    Date getStartDate() throws ParseException {
        return Constants.dateTimeFormat.parse(dateInput.getText());
    }

    /**
     * Translate the common elements
     */
    void translateCommon() {
        nameInput.setPromptText(language.get(Lang.NAME_PROMPT));
        nameLabel.setText(language.get(Lang.NAME_LABEL));

        rateLabel.setText(language.get(Lang.RATE_LABEL_DEVICE));
        resolutionLabel.setText(language.get(Lang.RESOLUTION_LABEL));
        startLabel.setText(language.get(Lang.START_DATE_LABEL));
        highLabel.setText(language.get(Lang.HIGH_ALARM_LABEL));
        lowLabel.setText(language.get(Lang.LOW_ALARM_LABEL));

        if (Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C)) {
            alarmLabel.setText(language.get(Lang.SET_ALARM_LABEL_C));
        } else {
            alarmLabel.setText(language.get(Lang.SET_ALARM_LABEL_F));
        }

        observationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));

        immediatelyCheck.setText(language.get(Lang.IMMEDIATELY));
        onDateCheck.setText(language.get(Lang.ON_DATE));
        onAlarmCheck.setText(language.get(Lang.ON_ALARM));
        delayCheck.setText(language.get(Lang.ON_DELAY));

        syncTime.setText(language.get(Lang.SYNC_CHECK));
        rollOver.setText(language.get(Lang.ROLL_OVER_CHECK));
        activateAlarmCheck.setText(language.get(Lang.SET_ALARM_CHECK));

        rateInput.setPromptText(language.get(Lang.RATE_PROMPT));
        observationsArea.setPromptText(language.get(Lang.OBSERVATIONS_PROMPT));
    }
}
