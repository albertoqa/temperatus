package temperatus.controller.creation;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.RangeSlider;
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
import java.util.*;
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

    @FXML private RangeSlider rangeSlider;
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

    private static final int NUMBER_OF_TICKS = 8;    // ticks to show on the slider
    private static final String HEADER = "Date/Time,Unit,Value";    // Header line where the measurements start in the csv

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();
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
     * @param data list of data for each position
     * @param generalData general data
     * @param controller controller of the newRecord to setUp the update if necessary
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
     * Set rangeSlider and textInputs to init and end date
     */
    private void loadTimeRange() {
        rangeSlider.setMax(generalData.getEndDate().getTime());
        rangeSlider.setMin(generalData.getStartDate().getTime());
        rangeSlider.setHighValue(generalData.getEndDate().getTime());
        rangeSlider.setLowValue(generalData.getStartDate().getTime());
        rangeSlider.setLabelFormatter(new DateStringConverter(true));   // timeFormat
        rangeSlider.setShowTickMarks(true);
        rangeSlider.setShowTickLabels(true);

        long timeInBetween = generalData.getEndDate().getTime() - generalData.getStartDate().getTime();
        int majorTickUnit = (int) timeInBetween / NUMBER_OF_TICKS;

        rangeSlider.setMajorTickUnit(majorTickUnit);

        Bindings.bindBidirectional(initTime.textProperty(), rangeSlider.lowValueProperty(), new DateStringConverter(false));    // dateTimeFormat
        Bindings.bindBidirectional(endTime.textProperty(), rangeSlider.highValueProperty(), new DateStringConverter(false));
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
        newRecordController.getUpdateReady();
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
                    final Date startDate;
                    final Date endDate;

                    startDate = Constants.dateTimeFormat.parse(initTime.getText());
                    endDate = Constants.dateTimeFormat.parse(endTime.getText());

                    // Calculate total number of measurements to update the progress indicator
                    long totalMeasurements = 0;
                    for (ValidatedData validatedData : data) {
                        totalMeasurements += validatedData.getMeasurements().stream().filter(measurement -> measurement.getDate().before(endDate) && measurement.getDate().after(startDate)).count();
                    }

                    // Save measurements + check if is in the range
                    int actualMeasurement = 0;
                    for (ValidatedData validatedData : data) {
                        //if (!validatedData.isUpdate()) { // only save if it is not an update

                            BufferedReader file = new BufferedReader(new FileReader(validatedData.getDataFile()));
                            String line;
                            String input = Constants.EMPTY;

                            // Write the mission data until we found the Header line
                            while ((line = file.readLine()) != null) {
                                input += line + System.lineSeparator();
                                if(line.contains(HEADER)) {
                                    break;
                                }
                            }

                            // insert only measurements in the range >= startDate and <= endDate
                            for (Measurement measurement : validatedData.getMeasurements()) {
                                if ((measurement.getDate().after(startDate) && measurement.getDate().before(endDate)) || measurement.getDate().equals(endDate) || measurement.getDate().equals(startDate)) {
                                    updateProgress(actualMeasurement++, totalMeasurements);
                                    String toAdd = Constants.dateTimeCSVFormat.format(measurement.getDate()) + Constants.COMMA + Constants.UNIT_C + Constants.COMMA + Constants.decimalFormat.format(measurement.getData());
                                    toAdd = toAdd.replace(Constants.DOT, Constants.COMMA);
                                    input = input.concat(toAdd + System.lineSeparator());
                                }
                            }

                            FileOutputStream os = new FileOutputStream(validatedData.getDataFile());
                            os.write(input.getBytes());

                            file.close();
                            os.close();

                       /* } else {
                            for (Measurement measurement : validatedData.getMeasurements()) {
                                if (measurement.getDate().before(startDate) && measurement.getDate().after(endDate)) {
                                    updateProgress(actualMeasurement++, totalMeasurements);
                                    //measurementService.delete(measurement); // TODO is this working??
                                }
                            }
                        }*/
                    }

                    Set<Formula> selectedFormulas = new HashSet<>();
                    selectedFormulas.addAll(listViewFormulas.getCheckModel().getCheckedItems());

                    mission.setFormulas(selectedFormulas);
                    missionService.saveOrUpdate(mission);   // update mission saving the selected formulas

                    updateProgress(10, 10);


                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, e.getMessage());
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
            missionInfoController.setData(mission.getId());

            stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            anchorPane.setDisable(false);
        });

        saveMeasurementsAndFormulasForMissionTask.setOnFailed(event -> {

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
     * Create a new formula
     */
    @FXML
    private void addFormula() {
        VistaNavigator.openModal(Constants.NEW_FORMULA, Constants.EMPTY);
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