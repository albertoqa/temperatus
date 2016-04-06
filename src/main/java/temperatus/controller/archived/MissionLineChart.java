package temperatus.controller.archived;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.controlsfx.control.CheckListView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 6/4/16.
 */
@Controller
@Scope("prototype")
public class MissionLineChart implements Initializable, AbstractController{

    @FXML private LineChart<Date, Number> lineChart;
    @FXML private CheckListView<XYChart.Series<Date, Number>> iButtonsList;

    private HashMap<Record, List<Measurement>> dataMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lineChart.setAnimated(false);

    }

    public void setData(HashMap<Record, List<Measurement>> dataMap) {
        this.dataMap = dataMap;
        setLineChartData();

    }

    private void setLineChartData() {
        for(Record record: dataMap.keySet()) {
            List<Measurement> measurements = dataMap.get(record);

            XYChart.Series<Date, Number> serie = new XYChart.Series<Date, Number>();
            serie.setName(record.getPosition().getPlace());  //TODO change to Position name
            measurements.stream().forEach((measurement) -> {
                serie.getData().add(new XYChart.Data<Date, Number>(measurement.getDate(), measurement.getData()));
            });
            lineChart.getData().add(serie);
            iButtonsList.getItems().add(serie);
        }
        iButtonsList.getCheckModel().checkAll();

        iButtonsList.getCheckModel().getCheckedItems().addListener(new ListChangeListener<XYChart.Series<Date, Number>>() {
            public void onChanged(ListChangeListener.Change<? extends XYChart.Series<Date, Number>> c) {
                reloadChart();
            }
        });
    }

    private void reloadChart() {
        lineChart.getData().clear();
        lineChart.getData().addAll(iButtonsList.getCheckModel().getCheckedItems());
    }

    @Override
    public void translate() {

    }
}
