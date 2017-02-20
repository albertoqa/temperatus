package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.controlsfx.control.CheckListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.exporter.MissionExporter;
import temperatus.lang.Lang;
import temperatus.model.pojo.*;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;
import temperatus.util.FileUtils;
import temperatus.util.SpinnerFactory;
import temperatus.util.VistaNavigator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

    @FXML private CheckBox separateWithTags;

    private Mission mission;
    private HashMap<Record, List<Measurement>> dataMap;

    private static Logger logger = LoggerFactory.getLogger(ExportConfigurationController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();

        mission = null;
        SpinnerFactory.setIntegerSpinner(periodSpinner, 1);
    }

    /**
     * Cancel the export and close the window
     */
    @FXML
    private void cancel() {
        VistaNavigator.closeModal(titledPane);
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
     * Set the record-measurements information
     * @param dataMap record-measurements
     */
    void setDataMap(HashMap<Record, List<Measurement>> dataMap) {
        this.dataMap = dataMap;
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
        formulaCheckListView.getCheckModel().checkAll();
    }

    /**
     * Show a fileChooser and export selected positions/missions to the chosen file
     * @throws IOException
     */
    @FXML
    private void export() throws IOException {
        logger.info("Exporting mission data...");

        File file = FileUtils.saveExcelDialog(titledPane.getScene().getWindow());
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

            // Export the data using the preferred unit
            Unit unit = Constants.prefs.get(Constants.UNIT, Constants.UNIT_C).equals(Constants.UNIT_C) ? Unit.C: Unit.F;

            // create a new mission exporter and set the data to export
            MissionExporter missionExporter = new MissionExporter();
            missionExporter.setData(periodSpinner.getValue(), separateWithTags.isSelected(), mission.getName(), records, formulaCheckListView.getCheckModel().getCheckedItems(), dataMap, unit);

            FileUtils.writeDataToFile(file, missionExporter.export());

            // set default directory to current
            VistaNavigator.directory = file.getParent();
        }
        cancel();      // close the window
    }

    @Override
    public void translate() {
        titledPane.setText(language.get(Lang.EXPORT_TITLE));
        exportButton.setText(language.get(Lang.EXPORT));
        cancelButton.setText(language.get(Lang.CANCEL));
        periodLabel.setText(language.get(Lang.PERIOD));
        positionsLabel.setText(language.get(Lang.POSITIONS));
        formulasLabel.setText(language.get(Lang.FORMULAS));
        separateWithTags.setText(language.get(Lang.SEPARATE_WITH_TAGS));
    }
}
