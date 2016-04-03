package temperatus.controller.creation;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;
import org.controlsfx.control.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.pojo.GeneralData;
import temperatus.analysis.pojo.ValidatedData;
import temperatus.controller.archived.MissionInfoController;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.types.ListViewItem;
import temperatus.model.service.FormulaService;
import temperatus.model.service.MeasurementService;
import temperatus.model.service.MissionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by alberto on 7/2/16.
 */
@Controller
@Scope("prototype")
public class RecordConfigController extends AbstractCreationController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Tab generalTab;
    @FXML private Button addFormulaButton;
    @FXML private ListView<ListViewItem> listViewFormulas;

    @FXML private RangeSlider rangeSlider;
    @FXML private Spinner initSpinner;
    @FXML private Spinner endSpinner;

    @Autowired MeasurementService measurementService;
    @Autowired FormulaService formulaService;
    @Autowired MissionService missionService;

    static Logger logger = LoggerFactory.getLogger(NewProjectController.class.getName());

    private List<ValidatedData> data;
    private Mission mission;  // Parent mission

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    /**
     * All iButtons must be already saved to DB, measurements will be saved by this controller
     */
    public void setData(List<ValidatedData> data, GeneralData generalData) {
        this.data = data;
        loadFormulas();
        loadData();
    }

    /**
     * Create a new tab for each iButton with all its info
     */
    private void loadData() {





        /*for (Map.Entry<Ibutton, List<Measurement>> entry : dataMap.entrySet()) {

            Tab recordInfoTab = new Tab();

            // Load a new pane for the tab
            Node recordInfoPane = VistaNavigator.loader.load(RecordConfigController.class.getResource(Constants.RECORD_INFO));
            RecordInfoPaneController recordInfoPaneController = VistaNavigator.loader.getController();

            // Gather information related to iButton/Measurements
            Ibutton ibutton = entry.getKey();
            List<Measurement> measurements = entry.getValue();

            String model = ibutton.getModel();
            String sampleRate = "";
            String startTime = "";
            String stopTime = "";
            String totalMeasurements = String.valueOf(measurements.size());
            String maxTemp = "";
            String minTemp = "";

            // Store information in loaded pane
            recordInfoPaneController.setData(model, sampleRate, startTime, stopTime, totalMeasurements, maxTemp, minTemp);

            recordInfoTab.setContent(recordInfoPane);
            recordInfoTab.setText(ibutton.getSerial());

            // Add new tab
            tabPane.getTabs().add(recordInfoTab);
        }*/
    }

    private void loadTimeRange() {

    }

    /**
     * Load all formulas from DB
     * Pre-select default formulas for this game
     */
    private void loadFormulas() {
        List<Formula> formulas = formulaService.getAll();
        Set<Formula> defaultFormulas = mission.getGame().getFormulas();

        List<ListViewItem> items = new ArrayList<>();
        for (Formula formula : formulas) {
            boolean checked = defaultFormulas.contains(formula);
            items.add(new ListViewItem(formula.getName(), checked));
        }

        listViewFormulas.getItems().addAll(items);
        listViewFormulas.setCellFactory(CheckBoxListCell.forListView(new Callback<ListViewItem, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(ListViewItem item) {
                return item.onProperty();
            }
        }));
    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    @FXML
    void save() {

        /*for (List<Measurement> measurements : dataMap.values()) {
            for (Measurement measurement : measurements) {
                measurementService.save(measurement);
            }
        }*/

        MissionInfoController missionInfoController = VistaNavigator.loadVista(Constants.MISSION_INFO);
        // missionInfoController.setData(missionId);
    }

    @Override
    public void translate() {

    }


}
