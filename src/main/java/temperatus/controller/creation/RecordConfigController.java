package temperatus.controller.creation;

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
import temperatus.controller.archived.MissionInfoController;
import temperatus.exception.ControlledTemperatusException;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Position;
import temperatus.model.service.FormulaService;
import temperatus.model.service.MeasurementService;
import temperatus.model.service.MissionService;
import temperatus.util.Constants;
import temperatus.util.DateStringConverter;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.text.ParseException;
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
    @FXML private Button addFormulaButton;
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

    @Autowired MeasurementService measurementService;
    @Autowired FormulaService formulaService;
    @Autowired MissionService missionService;

    private static Logger logger = LoggerFactory.getLogger(RecordConfigController.class.getName());

    private List<ValidatedData> data;   // Complete data
    private GeneralData generalData;    // General data of the experiment
    private Mission mission;            // Parent mission

    private static final int NUMBER_OF_TICKS = 8;    // ticks to show on the slider

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
     * All iButtons must be already saved to DB, measurements will be saved by this controller
     */
    public void setData(List<ValidatedData> data, GeneralData generalData) {
        this.data = data;
        this.generalData = generalData;
        loadFormulas();     // load all formulas that can be applied to this mission
        loadData();         // load all data to configure and validate
        loadTimeRange();    // load the slider and set tick time and time range

        // TODO create popup to show problematic data and manage it
        // TODO modal window con todos los datos chungos
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
            String startDate = validatedData.getStartDate().toString();
            String endDate = validatedData.getFinishDate().toString();
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
        startDateLabel.setText(generalData.getStartDate().toString());
        endDateLabel.setText(generalData.getEndDate().toString());
        avgMeasurementsLabel.setText(String.valueOf(generalData.getMeasurementsPerButton()));
        maxTempLabel.setText(String.valueOf(generalData.getMaxTemp()));
        minTempLabel.setText(String.valueOf(generalData.getMinTemp()));
        avgTempLabel.setText(String.valueOf(generalData.getAvgTemp()));
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
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.NOT_APPLICABLE_FORMULA));
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() != ButtonType.OK) {
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

                Date startDate = null;
                Date endDate = null;

                try {
                    startDate = Constants.dateTimeFormat.parse(initTime.getText());
                    endDate = Constants.dateTimeFormat.parse(endTime.getText());
                } catch (ParseException e) {
                    logger.error("Incorrect dates...");
                    throw new InterruptedException();
                }

                // Calculate total number of measurements to update the progress indicator
                int totalMeasurements = 0;
                for (ValidatedData validatedData : data) {
                    for(Measurement measurement: validatedData.getMeasurements()) {
                        if(measurement.getDate().before(endDate) && measurement.getDate().after(startDate)) {
                            totalMeasurements++;
                        }
                    }
                }

                // Save measurements + check if is in the range - [the save is the slowest part]
                int actualMeasurement = 0;
                for (ValidatedData validatedData : data) {
                    for (Measurement measurement : validatedData.getMeasurements()) {
                        if (measurement.getDate().after(startDate) && measurement.getDate().before(endDate)) {
                            updateProgress(actualMeasurement++, totalMeasurements);
                            measurementService.save(measurement);
                        }
                    }
                }

                Set<Formula> selectedFormulas = new HashSet<>();
                selectedFormulas.addAll(listViewFormulas.getCheckModel().getCheckedItems());

                mission.setFormulas(selectedFormulas);
                try {
                    missionService.saveOrUpdate(mission);   // update mission saving the selected formulas
                } catch (ControlledTemperatusException e) {
                    logger.error("Error updating formulas of mission...");
                }

                updateProgress(10, 10);
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

        anchorPane.setDisable(true);

        VBox box = new VBox(pForm);
        box.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(box);

        Thread thread = new Thread(saveMeasurementsAndFormulasForMissionTask);
        thread.start();

        logger.info("Saving measurements to database...");
    }

    @Override
    public void translate() {
        generalTab.setText(language.get(Lang.GENERAL_TAB));
        addFormulaButton.setText(language.get(Lang.ADD_FORMULA_BUTTON));
        backButton.setText(language.get(Lang.BACK_BUTTON));
        timeRangeWarning.setText(language.get(Lang.TIME_RANGE_WARNING));
        titleLabel.setText(language.get(Lang.RECORD_CONFIG_TITLE));
        formulasLabel.setText(language.get(Lang.FORMULAS));
        rangeOfTimeLabel.setText(language.get(Lang.RANGE_OF_TIME));
        modelL.setText(language.get(Lang.MODELLABEL));
        rateL.setText(language.get(Lang.RATELABEL));
        startDateL.setText(language.get(Lang.START_DATE));
        endDateL.setText(language.get(Lang.END_DATE));
        avgMeasurementsL.setText(language.get(Lang.AVG_MEASUREMENTS));
        maxTempL.setText(language.get(Lang.MAX_TEMP));
        minTempL.setText(language.get(Lang.MIN_TEMP));
        avgTempL.setText(language.get(Lang.AVG_TEMP));
    }

}