package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;
import org.apache.poi.ss.usermodel.Workbook;
import org.controlsfx.control.CheckListView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exporter.MissionExporter;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.Record;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

/**
 * Created by alberto on 12/4/16.
 */
@Controller
@Scope("prototype")
public class ExportConfigurationController implements Initializable {

    @FXML TitledPane titledPane;
    @FXML Button exportButton;
    @FXML Button cancelButton;

    @FXML CheckListView<Formula> formulaCheckListView;
    @FXML CheckListView<Position> positionCheckListView;

    @FXML Spinner<Integer> periodSpinner;
    @FXML Label periodLabel;

    private Mission mission;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setSpinner();
    }

    @FXML
    private void cancel() {
        VistaNavigator.closeModal(titledPane);
        VistaNavigator.baseController.selectBase();
    }

    public void setMission(Mission mission) {
        this.mission = mission;
        loadData();
    }

    private void loadData() {
        formulaCheckListView.getItems().addAll(mission.getFormulas());
        for (Record record : mission.getRecords()) {
            positionCheckListView.getItems().add(record.getPosition());
        }
        positionCheckListView.getCheckModel().checkAll();
    }

    private void setSpinner() {
        // get a localized format for parsing
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };

        TextFormatter<Integer> priceFormatter = new TextFormatter<Integer>(
                new IntegerStringConverter(), 1, filter);

        periodSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, 10000, Integer.parseInt("1")));
        periodSpinner.setEditable(true);
        periodSpinner.getEditor().setTextFormatter(priceFormatter);
    }

    @FXML
    private void export() throws IOException {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLS (*.xls)", "*.xls");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {

            List<Record> records = new ArrayList<>();
            for(Position position: positionCheckListView.getCheckModel().getCheckedItems()) {
                for(Record record: mission.getRecords()) {
                    if(record.getPosition().equals(position)) {
                        records.add(record);
                        break;
                    }
                }
            }

            MissionExporter missionExporter = new MissionExporter();
            missionExporter.setData(periodSpinner.getValue(), mission.getName(), records, formulaCheckListView.getCheckModel().getCheckedItems(), mission.getRecords());

            Workbook workBook = missionExporter.export();

            FileOutputStream fileOut = new FileOutputStream(file);
            workBook.write(fileOut);
            fileOut.close();
        }

        cancel();
    }


    public void translate() {

    }

}
