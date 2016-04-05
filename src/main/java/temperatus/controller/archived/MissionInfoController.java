package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.model.pojo.*;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by alberto on 30/1/16.
 */
@Controller
@Scope("prototype")
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

    private Project project;
    private Mission mission;
    private Game game;
    private Subject subject;

    private HashMap<Record, List<Measurement>> dataMap;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setData(Mission mission) {
        this.mission = mission;
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
    private void exportData() {

    }
}
