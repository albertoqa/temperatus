package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.archived.MissionInfoController;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Measurement;
import temperatus.model.service.MeasurementService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by alberto on 7/2/16.
 */
@Controller
@Scope("prototype")
public class RecordConfigController extends AbstractCreationController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private ListView listViewFormulas;

    @Autowired MeasurementService measurementService;

    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    private HashMap<Ibutton, List<Measurement>> dataMap;
    private List<Formula> formulas;
    private int missionId;  // Parent mission

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * All iButtons must be already saved to DB, measurements will be saved by this controller
     *
     * @param dataMap
     */
    public void setDataMap(HashMap<Ibutton, List<Measurement>> dataMap) {
        this.dataMap = dataMap;

        loadData();
        loadFormulas();
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    /**
     * Create a new tab for each iButton with all its info
     */
    private void loadData() {
        for (Map.Entry<Ibutton, List<Measurement>> entry : dataMap.entrySet()) {

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
        }
    }

    /**
     * Load all formulas from DB
     * Pre-select default formulas for this game and put them on the top of the list
     */
    private void loadFormulas() {

    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    @FXML
    void save() {

        for (List<Measurement> measurements : dataMap.values()) {
            for (Measurement measurement : measurements) {
                measurementService.save(measurement);
            }
        }

        MissionInfoController missionInfoController = VistaNavigator.loadVista(Constants.MISSION_INFO);
       // missionInfoController.setData(missionId);
    }

    @Override
    public void translate() {

    }


}
