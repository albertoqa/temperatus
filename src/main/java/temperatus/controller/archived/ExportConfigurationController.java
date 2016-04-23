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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.exporter.MissionExporter;
import temperatus.lang.Lang;
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
 * Show export options to the user previously to save the exported data
 * <p>
 * Created by alberto on 12/4/16.
 */
@Controller
@Scope("prototype")
public class ExportConfigurationController implements Initializable, AbstractController {

    @FXML private TitledPane titledPane;
    @FXML private Button exportButton;
    @FXML private Button cancelButton;

    @FXML private CheckListView<Formula> formulaCheckListView;
    @FXML private CheckListView<Position> positionCheckListView;

    @FXML private Spinner<Integer> periodSpinner;
    @FXML private Label periodLabel;
    @FXML private Label positionsLabel;
    @FXML private Label formulasLabel;

    private Mission mission;

    private static Logger logger = LoggerFactory.getLogger(ExportConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        mission = null;
        IntegerSpinner.setSpinner(periodSpinner);
    }

    /**
     * Cancel the export and close the window
     */
    @FXML
    private void cancel() {
        VistaNavigator.closeModal(titledPane);
        VistaNavigator.baseController.selectBase();
    }

    /**
     * Set the mission that should be exported
     * @param mission mission to export
     */
    public void setMission(Mission mission) {
        this.mission = mission;
        loadData();
    }

    /**
     * Load the data of the mission - positions and formulas
     * The user can choose which positions and formulas use for export the data
     */
    private void loadData() {
        formulaCheckListView.getItems().addAll(mission.getFormulas());
        for (Record record : mission.getRecords()) {
            positionCheckListView.getItems().add(record.getPosition());
        }
        positionCheckListView.getCheckModel().checkAll();
    }

    /**
     * Show a fileChooser and export selected positions/missions to the chosen file
     * @throws IOException
     */
    @FXML
    private void export() throws IOException {
        logger.info("Exporting mission data...");

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLS (*.xls)", "*.xls");
        fileChooser.getExtensionFilters().add(extFilter);   //Set extension filter

        File file = fileChooser.showSaveDialog(null);   //Show save file dialog

        if (file != null) {
            List<Record> records = new ArrayList<>();
            for (Position position : positionCheckListView.getCheckModel().getCheckedItems()) {
                for (Record record : mission.getRecords()) {
                    if (record.getPosition().equals(position)) {
                        records.add(record);
                        break;
                    }
                }
            }

            // create a new mission exporter and set the data to export
            MissionExporter missionExporter = new MissionExporter();
            missionExporter.setData(periodSpinner.getValue(), mission.getName(), records, formulaCheckListView.getCheckModel().getCheckedItems(), mission.getRecords());

            Workbook workBook = missionExporter.export();

            FileOutputStream fileOut = new FileOutputStream(file);  // write generated data to a file
            workBook.write(fileOut);
            fileOut.close();
        }
        cancel();      // close the window
    }

    @Override
    public void translate() {
        exportButton.setText(language.get(Lang.EXPORT));
        cancelButton.setText(language.get(Lang.CANCEL));
        periodLabel.setText(language.get(Lang.PERIOD));
        positionsLabel.setText(language.get(Lang.POSITIONLABEL));
        formulasLabel.setText(language.get(Lang.FORMULAS));
    }
}
