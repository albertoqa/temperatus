package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.IButtonDataAnalysis;
import temperatus.controller.AbstractController;
import temperatus.exception.ControlledTemperatusException;
import temperatus.importer.IbuttonDataImporter;
import temperatus.lang.Lang;
import temperatus.model.pojo.*;
import temperatus.model.service.MissionService;
import temperatus.util.Constants;
import temperatus.util.KeyValidator;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Show the resume of a mission. General information, temperature graph and histogram
 * <p>
 * Created by alberto on 30/1/16.
 */
@Controller
@Scope("prototype")
public class MissionInfoController implements Initializable, AbstractController {

    @FXML private Label headerLabel;
    @FXML private Label projectNameLabel;
    @FXML private Label projectDateLabel;
    @FXML private Label missionLabel;
    @FXML private Label dateLabel;
    @FXML private Label authorLabel;
    @FXML private Label numberButtonsLabel;
    @FXML private Label observationsLabel;
    @FXML private Label gameLabel;
    @FXML private Label subjectLabel;

    @FXML private Label projectName;
    @FXML private Label projectDate;
    @FXML private Label missionName;
    @FXML private Label missionDate;
    @FXML private Label missionObservations;
    @FXML private Label missionAuthor;
    @FXML private Label gameName;
    @FXML private Label gameIbuttonsNumber;
    @FXML private Label subjectName;

    @FXML private Button exportButton;

    @FXML private StackPane lineChartStackPane;
    @FXML private StackPane histogramStack;

    @Autowired MissionService missionService;

    private Project project;
    private Mission mission;
    private Game game;
    private Subject subject;

    private HashMap<Record, List<Measurement>> dataMap;

    private static Logger logger = LoggerFactory.getLogger(MissionInfoController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing mission info controller");

        translate();
        VistaNavigator.setController(this);
    }

    /**
     * Set the mission data and load all its information
     *
     * @param missionId id of the mission to show
     */
    public void setData(int missionId) {
        this.mission = missionService.getById(missionId);
        project = mission.getProject();
        game = mission.getGame();
        subject = mission.getSubject();

        List<Record> records = mission.getRecords().stream().collect(Collectors.toList());

        boolean allRight = true;
        dataMap = new HashMap<>();
        for (Record record : records) {
            try {
                List<Measurement> measurements = new IbuttonDataImporter(new File(record.getDataPath())).getMeasurements();
                dataMap.put(record, measurements);
            } catch (ControlledTemperatusException e) {
                allRight = false;
                logger.warn("Error reading data for the mission, some csv is corrupted or doesn't exists");
            }
        }

        if(!allRight) {
            VistaNavigator.showAlert(Alert.AlertType.WARNING, language.get(Lang.CORRUPTED_DATA_IMPORT));
        }

        writeDataOnView();
    }

    /**
     * Load the mission data on the view: labels, graph and histogram
     */
    private void writeDataOnView() {
        projectName.setText(project.getName());
        projectDate.setText(Constants.dateFormat.format(project.getDateIni()));

        missionName.setText(mission.getName());
        missionDate.setText(Constants.dateFormat.format(mission.getDateIni()));
        missionAuthor.setText(mission.getAuthor().getName());
        missionObservations.setText(mission.getObservations());

        gameName.setText(game.getTitle());
        gameIbuttonsNumber.setText(String.valueOf(game.getNumButtons()));

        subjectName.setText(subject.getName());

        // Load and add the temperature graph pane
        Node missionLineChartPane = VistaNavigator.loader.load(MissionLineChart.class.getResource(Constants.MISSION_LINE_CHART));
        MissionLineChart missionLineChart = VistaNavigator.loader.getController();
        missionLineChart.setData(dataMap, mission.getFormulas());

        lineChartStackPane.getChildren().setAll(missionLineChartPane);

        // Get the necessary info for the histogram and show it
        List<List<Measurement>> measurementsLists = dataMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        double maxT = Double.MIN_VALUE;
        double minT = Double.MAX_VALUE;

        // Calculate the max and min temperature of the mission
        for (List<Measurement> measurementList : measurementsLists) {
            double max = IButtonDataAnalysis.getMaxTemperature(measurementList);
            double min = IButtonDataAnalysis.getMinTemperature(measurementList);

            if (max > maxT) {
                maxT = max;
            }
            if (min < minT) {
                minT = min;
            }
        }

        // Load and add the histogram pane
        Node histogramChart = VistaNavigator.loader.load(HistogramController.class.getResource(Constants.HISTOGRAM_CHART));
        HistogramController histogramController = VistaNavigator.loader.getController();
        histogramController.setData(measurementsLists, maxT, minT);

        histogramStack.getChildren().setAll(histogramChart);
    }

    /**
     * Export the mission data to a excel file
     * This option is only activated for premium version of the program
     *
     * @throws IOException
     */
    @FXML
    private void exportData() throws IOException {
        if(KeyValidator.checkActivationStatus()) {
            ExportConfigurationController exportConfigurationController = VistaNavigator.openModal(Constants.EXPORT_CONFIG, Constants.EMPTY);
            exportConfigurationController.setMission(mission);
            exportConfigurationController.setDataMap(dataMap);
        }
    }

    @Override
    public void translate() {
        headerLabel.setText(language.get(Lang.MISSION_INFO));
        projectDateLabel.setText(language.get(Lang.START_DATE_LABEL));
        projectNameLabel.setText(language.get(Lang.PROJECT_LABEL));
        missionLabel.setText(language.get(Lang.MISSION_LABEL));
        dateLabel.setText(language.get(Lang.START_DATE_LABEL));
        authorLabel.setText(language.get(Lang.AUTHOR_LABEL));
        observationsLabel.setText(language.get(Lang.OBSERVATIONS_LABEL));
        numberButtonsLabel.setText(language.get(Lang.NUMBER_OF_BUTTONS_LABEL));
        gameLabel.setText(language.get(Lang.GAME_LABEL));
        subjectLabel.setText(language.get(Lang.SUBJECT_LABEL));
        exportButton.setText(language.get(Lang.EXPORT));
    }
}
