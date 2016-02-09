package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.pojo.*;
import temperatus.model.service.*;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 30/1/16.
 */
@Component
public class MissionInfoController implements Initializable {

    @FXML private Label projectName;
    @FXML private Label projectDate;
    @FXML private Label projectObservations;
    @FXML private Label missionName;
    @FXML private Label missionDate;
    @FXML private Label missionObservations;
    @FXML private Label missionAuthor;
    @FXML private Label gameName;
    @FXML private Label gameIbuttonsNumber;
    @FXML private Label gameObservations;
    @FXML private Label subjectName;

    @FXML private LineChart<Date, Number> lineChart;

    @Autowired ProjectService projectService;
    @Autowired MissionService missionService;
    @Autowired GameService gameService;
    @Autowired SubjectService subjectService;
    @Autowired RecordService recordService;
    @Autowired MeasurementService measurementService;

    private Project project;
    private Mission mission;
    private Game game;
    private Subject subject;

    private HashMap<Record, List<Measurement>> dataMap;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setData(int missionId) {
        mission = missionService.getById(missionId);
        project = mission.getProject();
        game = mission.getGame();
        subject = mission.getSubject();

        List<Record> records = recordService.getByMissionId(missionId);

        dataMap = new HashMap<>();
        for(Record record: records) {
            List<Measurement> measurements = measurementService.getAllByRecordId(record.getId());
            dataMap.put(record, measurements);
        }

        //TODO throw exception if == null

        writeDataOnView();
    }

    private void writeDataOnView() {
        projectName.setText(project.getName());
        projectDate.setText(project.getDateIni().toString());
        projectObservations.setText(project.getObservations());

        missionName.setText(mission.getName());
        missionDate.setText(mission.getDateIni().toString());
        missionAuthor.setText(mission.getAuthor().getName());
        missionObservations.setText(mission.getObservations());

        gameName.setText(game.getTitle());
        gameIbuttonsNumber.setText(String.valueOf(game.getNumButtons()));
        gameObservations.setText(game.getObservations());

        subjectName.setText(subject.getName());

        for(Record record: dataMap.keySet()) {
            List<Measurement> measurements = dataMap.get(record);

            XYChart.Series<Date, Number> series = new XYChart.Series<Date, Number>();
            series.setName(record.getPosition().getPlace());  //TODO change to Position name
            measurements.stream().forEach((measurement) -> {
                series.getData().add(new XYChart.Data<Date, Number>(measurement.getDate(), measurement.getData()));
            });
            lineChart.getData().add(series);

        }

    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }
}
