package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Workbook;
import org.controlsfx.control.CheckListView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.exporter.MissionExporter;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Position;
import temperatus.model.pojo.Record;
import temperatus.util.IntegerSpinner;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
        IntegerSpinner.setSpinner(periodSpinner);
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
