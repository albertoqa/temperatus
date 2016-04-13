package temperatus.controller.archived;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import org.controlsfx.control.CheckListView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.DateAxis;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by alberto on 6/4/16.
 */
@Controller
@Scope("prototype")
public class MissionLineChart implements Initializable, AbstractController {

    @FXML private LineChart<Date, Number> lineChart;
    @FXML private CheckListView<XYChart.Series<Date, Number>> iButtonsList;
    @FXML private CheckListView<XYChart.Series<Date, Number>> formulasList;
    @FXML private NumberAxis yAxis;
    @FXML private DateAxis xAxis;

    @FXML private Spinner<Integer> spinner;

    private ObservableList<XYChart.Series<Date, Number>> series;

    private HashMap<Record, List<Measurement>> dataMap;
    private List<Formula> formulas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        series = FXCollections.observableArrayList();
        lineChart.setData(series);
        lineChart.setAnimated(false);

        //lineChart.setCreateSymbols(false);
    }

    public void setData(HashMap<Record, List<Measurement>> dataMap, double minTemp, double maxTemp, Set<Formula> formulas) {
        this.dataMap = dataMap;
        this.formulas = new ArrayList<>(formulas);

        setLineChartDataPositions();

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(minTemp - 5);
        yAxis.setUpperBound(maxTemp + 5);
    }

    private void setLineChartDataFormulas() {

    }

    private void setLineChartDataPositions() {
        for (Record record : dataMap.keySet()) {
            List<Measurement> measurements = dataMap.get(record);

            XYChart.Series<Date, Number> serie = new XYChart.Series<Date, Number>();
            serie.setName(record.getPosition().getPlace());  //TODO change to Position name
            measurements.stream().forEach((measurement) -> {
                serie.getData().add(new XYChart.Data<Date, Number>(measurement.getDate(), measurement.getData()));
            });
            series.add(serie);
            //lineChart.getData().add(serie);
            iButtonsList.getItems().add(serie);
        }
        iButtonsList.getCheckModel().checkAll();

        iButtonsList.getCheckModel().getCheckedItems().addListener(new ListChangeListener<XYChart.Series<Date, Number>>() {
            public void onChanged(ListChangeListener.Change<? extends XYChart.Series<Date, Number>> c) {
                //reloadChart();
                //series.remove(c);
            }
        });

        /**
         * Browsing through the Data and applying ToolTip
         * as well as the class on hover
         */
        if (lineChart.getCreateSymbols()) {
            for (XYChart.Series<Date, Number> s : lineChart.getData()) {
                for (XYChart.Data<Date, Number> d : s.getData()) {
                    Tooltip.install(d.getNode(), new Tooltip(
                            d.getXValue().toString() + "\n" +
                                    "Temperature : " + d.getYValue()));

                    //Adding class on hover
                    d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover"));

                    //Removing class on exit
                    d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover"));
                }
            }
        }
    }

    private void reloadChart() {
        //lineChart.getData().clear();
        //lineChart.getData().addAll(iButtonsList.getCheckModel().getCheckedItems());
    }

    @FXML
    public void saveAsPng() {

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {

            WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                // TODO: handle exception here
            }
        }
    }

    @Override
    public void translate() {

    }
}
