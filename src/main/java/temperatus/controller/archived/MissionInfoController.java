package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
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
 * Created by alberto on 30/1/16.
 */
@Controller
@Scope("prototype")
public class MissionInfoController implements Initializable, AbstractController {

    @FXML private Label projectName;
    @FXML private Label projectDate;
    @FXML private Label missionName;
    @FXML private Label missionDate;
    @FXML private Label missionObservations;
    @FXML private Label missionAuthor;
    @FXML private Label gameName;
    @FXML private Label gameIbuttonsNumber;
    @FXML private Label subjectName;

    @FXML private StackPane lineChartStackPane;
    @FXML private StackPane histogramStack;

    @Autowired MissionService missionService;

    private Project project;
    private Mission mission;
    private Game game;
    private Subject subject;

    private HashMap<Record, List<Measurement>> dataMap;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    public void setData(int missionId) {
        this.mission = missionService.getById(missionId);
        project = mission.getProject();
        game = mission.getGame();
        subject = mission.getSubject();

        List<Record> records = mission.getRecords().stream().collect(Collectors.toList());

        try {
            dataMap = new HashMap<>();
            for (Record record : records) {
                List<Measurement> measurements = new IbuttonDataImporter(new File(record.getDataPath())).getMeasurements();
                dataMap.put(record, measurements);
            }
        } catch (ControlledTemperatusException e) {
            e.printStackTrace();
        }

        //TODO throw exception if == null

        writeDataOnView();
    }

    private void writeDataOnView() {
        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());

        missionName.setText(mission.getName());
        missionDate.setText(mission.getDateIni().toString());
        missionAuthor.setText(mission.getAuthor().getName());
        missionObservations.setText(mission.getObservations());

        gameName.setText(game.getTitle());
        gameIbuttonsNumber.setText(String.valueOf(game.getNumButtons()));

        subjectName.setText(subject.getName());

        Node missionLineChartPane = VistaNavigator.loader.load(MissionLineChart.class.getResource(Constants.MISSION_LINE_CHART));
        MissionLineChart missionLineChart = VistaNavigator.loader.getController();
        missionLineChart.setData(dataMap, mission.getFormulas());

        lineChartStackPane.getChildren().setAll(missionLineChartPane);

        List<List<Measurement>> measurementsLists = dataMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
        double maxT = Double.MIN_VALUE;
        double minT = Double.MAX_VALUE;
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

        Node histogramChart = VistaNavigator.loader.load(HistogramController.class.getResource(Constants.HISTOGRAM_CHART));
        HistogramController histogramController = VistaNavigator.loader.getController();
        histogramController.setData(measurementsLists, maxT, minT);

        histogramStack.getChildren().setAll(histogramChart);
    }

    @FXML
    private void exportData() throws IOException {
        // Only allow export if complete version of the application, trial version cannot export data
        if (Constants.prefs.getBoolean(Constants.ACTIVATED, false)) {
            ExportConfigurationController exportConfigurationController = VistaNavigator.openModal(Constants.EXPORT_CONFIG, language.get(Lang.EXPORTCONFIG));
            exportConfigurationController.setMission(mission);
            exportConfigurationController.setDataMap(dataMap);
        } else {
            VistaNavigator.openModal(Constants.BUY_COMPLETE, language.get(Lang.BUY_COMPLETE));
        }
    }

    @Override
    public void translate() {

    }
}
