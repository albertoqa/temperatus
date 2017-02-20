package temperatus.controller.creation;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import multirange.MultiRange;
import multirange.Range;
import org.controlsfx.control.CheckListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.FormulaUtil;
import temperatus.analysis.pojo.GeneralData;
import temperatus.analysis.pojo.ValidatedData;
import temperatus.calculator.Calculator;
import temperatus.controller.archived.MissionInfoController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.types.Unit;
import temperatus.model.service.FormulaService;
import temperatus.model.service.MissionService;
import temperatus.model.service.RecordService;
import temperatus.util.Constants;
import temperatus.util.DateStringConverter;
import temperatus.util.VistaNavigator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration of the experiments: range of time, formulas and wrong data.
 * After configuration, save everything to database.
 * <p>
 * Created by alberto on 7/2/16.
 */
@Controller
@Scope("prototype")
public class RecordConfigController extends AbstractCreationController implements Initializable {

    @FXML private StackPane stackPane;
    @FXML private AnchorPane anchorPane;

    @FXML private TabPane tabPane;
    @FXML private Tab generalTab;
    @FXML private Button backButton;
    @FXML private CheckListView<Formula> listViewFormulas;

    @FXML private MultiRange multiRange;
    @FXML private TextField initTime;
    @FXML private TextField endTime;
    @FXML private Label timeRangeWarning;
    @FXML private Label initTimeLabel;
    @FXML private Label endTimeLabel;

    @FXML private Label titleLabel;
    @FXML private Label formulasLabel;
    @FXML private Label rangeOfTimeLabel;

    @FXML private Label modelLabel;
    @FXML private Label rateLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label avgMeasurementsLabel;
    @FXML private Label maxTempLabel;
    @FXML private Label minTempLabel;
    @FXML private Label avgTempLabel;

    @FXML private Label modelL;
    @FXML private Label rateL;
    @FXML private Label startDateL;
    @FXML private Label endDateL;
    @FXML private Label avgMeasurementsL;
    @FXML private Label maxTempL;
    @FXML private Label minTempL;
    @FXML private Label avgTempL;

    @Autowired FormulaService formulaService;
    @Autowired MissionService missionService;
    @Autowired RecordService recordService;

    private static Logger logger = LoggerFactory.getLogger(RecordConfigController.class.getName());

    private List<ValidatedData> data;   // Complete data
    private GeneralData generalData;    // General data of the experiment
    private Mission mission;            // Parent mission

    private NewRecordController newRecordController;

    private DateStringConverter dateStringConverter;
    private DoubleProperty previousLowProperty;     // used to bind the textInput to the current selected range
    private DoubleProperty previousHighProperty;

    private static final int NUMBER_OF_TICKS = 8;    // ticks to show on the slider
    private static final String HEADER = "Date/Time,Unit,Value";    // Header line where the measurements start in the csv

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();
        dateStringConverter = new DateStringConverter(false);
    }

    /**
     * Set mission to configure
     *
     * @param mission mission to configure
     */
    public void setMission(Mission mission) {
        this.mission = mission;
    }

    /**
     * Set the data for the mission configuration
     *
     * @param data        list of data for each position
     * @param generalData general data
     * @param controller  controller of the newRecord to setUp the update if necessary
     */
    public void setData(List<ValidatedData> data, GeneralData generalData, NewRecordController controller) {
        this.data = data;
        this.generalData = generalData;
        this.newRecordController = controller;
        loadFormulas();     // load all formulas that can be applied to this mission
        loadData();         // load all data to configure and validate
        loadTimeRange();    // load the slider and set tick time and time range
    }

    /**
     * Create a new tab for each iButton with all its info
     */
    private void loadData() {
        loadGeneralData();      // the first tab show the general data of the experiment

        for (ValidatedData validatedData : data) {
            Tab recordInfoTab = new Tab();
            StackPane stackPane = new StackPane();

            // Load a new pane for the tab
            Node recordInfoPane = VistaNavigator.loader.load(RecordConfigController.class.getResource(Constants.RECORD_INFO));
            RecordInfoPaneController recordInfoPaneController = VistaNavigator.loader.getController();

            String model = validatedData.getDeviceModel();
            String serial = validatedData.getDeviceSerial();
            String alias = validatedData.getIbutton().getAlias();
            String sampleRate = validatedData.getSampleRate();
            String startDate = Constants.dateTimeFormat.format(validatedData.getStartDate());
            String endDate = Constants.dateTimeFormat.format(validatedData.getFinishDate());
            String totalMeasurements = String.valueOf(validatedData.getMeasurements().size());
            String position = validatedData.getPosition().getPlace();

            // Store information in loaded pane
            recordInfoPaneController.setData(model, serial, alias, sampleRate, startDate, endDate, totalMeasurements, position, validatedData.getMeasurements());

            stackPane.getChildren().setAll(recordInfoPane);
            recordInfoTab.setContent(stackPane);

            if (alias != null && !alias.isEmpty()) {
                recordInfoTab.setText(alias);
            } else {
                recordInfoTab.setText(serial);
            }

            // Add new tab
            tabPane.getTabs().add(recordInfoTab);
        }
    }

    /**
     * Set multiRange and textInputs to init and end date
     */
    private void loadTimeRange() {

        long max = generalData.getEndDate().getTime();
        long min = generalData.getStartDate().getTime();

        multiRange.setMin(min);
        multiRange.setMax(max);
        multiRange.setHighRangeValue(max);
        multiRange.setLowRangeValue(min);
        multiRange.setLabelFormatter(new DateStringConverter(true));   // timeFormat
        multiRange.setShowTickMarks(true);
        multiRange.setShowTickLabels(true);

        long timeInBetween = generalData.getEndDate().getTime() - generalData.getStartDate().getTime();
        int majorTickUnit = (int) timeInBetween / NUMBER_OF_TICKS;

        multiRange.setMajorTickUnit(majorTickUnit);

        initTime.editableProperty().setValue(true);
        endTime.editableProperty().setValue(true);

        initTime.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                multiRange.validateValues();
                multiRange.requestLayout();
            }
        });

        endTime.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                multiRange.validateValues();
                multiRange.requestLayout();
            }
        });

        Bindings.bindBidirectional(initTime.textProperty(), multiRange.getLowValueProperty(), dateStringConverter);    // dateTimeFormat
        Bindings.bindBidirectional(endTime.textProperty(), multiRange.getHighValueProperty(), dateStringConverter);

        previousLowProperty = multiRange.getLowValueProperty();
        previousHighProperty = multiRange.getHighValueProperty();

        multiRange.lowValueProperty().addListener((observable, oldValue, newValue) -> {
            bindTextToCurrentLowValue();
        });

        multiRange.highValueProperty().addListener((observable, oldValue, newValue) -> {
            bindTextToCurrentHighValue();
        });
    }

    /**
     * Unbind the text input of its previous binded property and bind it to the current low property
     */
    private void bindTextToCurrentLowValue() {
        initTime.textProperty().unbindBidirectional(previousLowProperty);
        previousLowProperty = multiRange.getLowValueProperty();
        initTime.textProperty().bindBidirectional(multiRange.getLowValueProperty(), dateStringConverter);
    }

    /**
     * Unbind the text input of its previous binded property and bind it to the current high property
     */
    private void bindTextToCurrentHighValue() {
        endTime.textProperty().unbindBidirectional(previousHighProperty);
        previousHighProperty = multiRange.getHighValueProperty();
        endTime.textProperty().bindBidirectional(multiRange.getHighValueProperty(), dateStringConverter);
    }

    /**
     * Show general data on screen (labels)
     */
    private void loadGeneralData() {
        modelLabel.setText(generalData.getModels());
        rateLabel.setText(generalData.getRate());
        startDateLabel.setText(Constants.dateTimeFormat.format(generalData.getStartDate()));
        endDateLabel.setText(Constants.dateTimeFormat.format(generalData.getEndDate()));
        avgMeasurementsLabel.setText(String.valueOf(generalData.getMeasurementsPerButton()));

        // Export the data using the preferred unit
        Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C : Unit.F;

        if (Constants.UNIT_C.equals(unit.name())) {
            maxTempLabel.setText(String.valueOf(Constants.decimalFormat.format(generalData.getMaxTemp())));
            minTempLabel.setText(String.valueOf(Constants.decimalFormat.format(generalData.getMinTemp())));
            avgTempLabel.setText(String.valueOf(Constants.decimalFormat.format(generalData.getAvgTemp())));
        } else {
            maxTempLabel.setText(String.valueOf(Constants.decimalFormat.format(Calculator.celsiusToFahrenheit(generalData.getMaxTemp()))));
            minTempLabel.setText(String.valueOf(Constants.decimalFormat.format(Calculator.celsiusToFahrenheit(generalData.getMinTemp()))));
            avgTempLabel.setText(String.valueOf(Constants.decimalFormat.format(Calculator.celsiusToFahrenheit(generalData.getAvgTemp()))));
        }
    }

    /**
     * Load all formulas from DB but allow to check only formulas that can be applied to this mission/game
     * Pre-select default formulas for this game
     */
    private void loadFormulas() {
        listViewFormulas.setItems(FXCollections.observableArrayList(formulaService.getAll()));
        for (Formula formula : mission.getGame().getFormulas()) {
            listViewFormulas.getCheckModel().check(formula);
        }

        listViewFormulas.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super Formula>) c -> {
            c.next();
            for (Formula formula : c.getAddedSubList()) {
                if (!isFormulaApplicableToThisMission(formula)) {
                    if (!VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.NOT_APPLICABLE_FORMULA))) {
                        listViewFormulas.getCheckModel().clearCheck(formula);
                        listViewFormulas.refresh();
                        break;
                    }
                }
            }
        });
    }

    /**
     * Check if the formula can be applied to the current mission/game
     *
     * @param formula formula to check
     * @return is valid to apply this formula to this mission?
     */
    private boolean isFormulaApplicableToThisMission(Formula formula) {
        List<Position> positions = data.stream().map(ValidatedData::getPosition).collect(Collectors.toList());
        return FormulaUtil.isValidFormula(formula.getOperation(), positions);
    }

    /**
     * Go back to the previous screen
     */
    @FXML
    private void back() {
        newRecordController.comingBackFromRecordConfig();
        VistaNavigator.popViewFromStack();
    }

    /**
     * Save measurements and mission-formulas to database
     */
    @Override
    @FXML
    void save() {
        ProgressIndicator pForm = new ProgressIndicator();
        Task<Void> saveMeasurementsAndFormulasForMissionTask = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {

                try {
                    List<Range> ranges = multiRange.getRanges();

                    // Calculate total number of measurements to update the progress indicator
                    long totalMeasurements = 0;
                    for (ValidatedData validatedData : data) {
                        for (Measurement measurement : validatedData.getMeasurements()) {
                            for (Range range : ranges) {
                                if (measurement.getDate().getTime() <= range.getHigh() && measurement.getDate().getTime() >= range.getLow()) {
                                    totalMeasurements++;
                                    break;
                                }
                            }
                        }
                    }

                    // Save measurements + check if is in the range
                    int actualMeasurement = 0;
                    for (ValidatedData validatedData : data) {
                        BufferedReader file = new BufferedReader(new FileReader(validatedData.getDataFile()));
                        String line;
                        StringBuilder input = new StringBuilder(Constants.EMPTY);

                        // Write the mission data until we found the Header line
                        while ((line = file.readLine()) != null) {
                            input.append(line).append(System.lineSeparator());
                            if (line.contains(HEADER)) {
                                break;
                            }
                        }

                        // insert only measurements in the range >= startDate and <= endDate
                        for (Measurement measurement : validatedData.getMeasurements()) {
                            for (Range range : ranges) {
                                if (measurement.getDate().getTime() <= range.getHigh() && measurement.getDate().getTime() >= range.getLow()) {
                                    updateProgress(actualMeasurement++, totalMeasurements);
                                    String toAdd = Constants.dateTimeFormat.format(measurement.getDate()) + Constants.COMMA + Constants.UNIT_C + Constants.COMMA + Constants.decimalFormat.format(measurement.getData());
                                    toAdd = toAdd.replace(Constants.DOT, Constants.COMMA);
                                    input.append(toAdd).append(System.lineSeparator());
                                    break;
                                }
                            }
                        }

                        FileOutputStream os = new FileOutputStream(validatedData.getDataFile());
                        os.write(input.toString().getBytes());

                        file.close();
                        os.close();
                    }

                    Set<Formula> selectedFormulas = new HashSet<>();
                    selectedFormulas.addAll(listViewFormulas.getCheckModel().getCheckedItems());

                    mission.setFormulas(selectedFormulas);
                    missionService.saveOrUpdate(mission);   // update mission saving the selected formulas

                    updateProgress(10, 10);

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        logger.error("Error saving data..." + e.getMessage());
                        stackPane.getChildren().remove(stackPane.getChildren().size() - 1); // remove the progress indicator
                        anchorPane.setDisable(false);
                    });

                    throw new InterruptedException();
                }
                return null;
            }
        };

        // binds progress of progress bars to progress of task:
        pForm.progressProperty().bind(saveMeasurementsAndFormulasForMissionTask.progressProperty());

        saveMeasurementsAndFormulasForMissionTask.setOnSucceeded(event -> {
            MissionInfoController missionInfoController = VistaNavigator.loadVista(Constants.MISSION_INFO);
            if (missionInfoController != null) {
                missionInfoController.setData(mission.getId());
            }

            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            anchorPane.setDisable(false);
        });

        saveMeasurementsAndFormulasForMissionTask.setOnFailed(event -> VistaNavigator.showAlert(Alert.AlertType.ERROR, language.get(Lang.ERROR_SAVING_MEASUREMENTS)));

        anchorPane.setDisable(true);

        VBox box = new VBox(pForm);
        box.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(box);

        Thread thread = new Thread(saveMeasurementsAndFormulasForMissionTask);
        thread.setDaemon(true);
        thread.start();

        logger.info("Saving measurements to database...");
    }

    /**
     * If a new formula is created, reload it
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Formula) {
            listViewFormulas.getItems().add((Formula) object);
        }
    }

    @Override
    public void translate() {
        generalTab.setText(language.get(Lang.GENERAL_TAB));
        backButton.setText(language.get(Lang.BACK_BUTTON));
        timeRangeWarning.setText(language.get(Lang.TIME_RANGE_WARNING));
        titleLabel.setText(language.get(Lang.RECORD_CONFIG_TITLE));
        formulasLabel.setText(language.get(Lang.FORMULAS));
        rangeOfTimeLabel.setText(language.get(Lang.RANGE_OF_TIME));
        modelL.setText(language.get(Lang.MODEL_LABEL));
        rateL.setText(language.get(Lang.RATE_LABEL));
        startDateL.setText(language.get(Lang.START_DATE));
        endDateL.setText(language.get(Lang.END_DATE));
        avgMeasurementsL.setText(language.get(Lang.AVG_MEASUREMENTS));
        maxTempL.setText(language.get(Lang.MAX_TEMP));
        minTempL.setText(language.get(Lang.MIN_TEMP));
        avgTempL.setText(language.get(Lang.AVG_TEMP));
    }

}