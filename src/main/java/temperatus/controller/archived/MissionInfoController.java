package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.*;
import temperatus.model.service.MissionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
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

        dataMap = new HashMap<>();
        for(Record record: records) {
            List<Measurement> measurements = record.getMeasurements().stream().collect(Collectors.toList());
            dataMap.put(record, measurements);
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
        missionLineChart.setData(dataMap);

        lineChartStackPane.getChildren().setAll(missionLineChartPane);
    }

    @FXML
    private void exportData() {

    }

    @Override
    public void translate() {

    }
}
