package temperatus.controller.creation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import temperatus.controller.archived.MissionInfoController;
import temperatus.model.pojo.Ibutton;
import temperatus.model.pojo.Measurement;
import temperatus.model.service.MeasurementService;
import temperatus.util.Constants;
import temperatus.util.SpringFxmlLoader;
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
public class RecordConfigController extends AbstractCreationController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private ListView listViewFormulas;

    @Autowired MeasurementService measurementService;

    static Logger logger = Logger.getLogger(NewProjectController.class.getName());

    private HashMap<Ibutton, List<Measurement>> dataMap;
    private int missionId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setDataMap(HashMap<Ibutton, List<Measurement>> dataMap) {
        this.dataMap = dataMap;

        loadData();
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    private void loadData() {
        for (Map.Entry<Ibutton, List<Measurement>> entry : dataMap.entrySet()) {

            Tab recordInfoTab = new Tab();
            SpringFxmlLoader loader = VistaNavigator.getLoader();
            Node recordInfoPane = loader.load(RecordConfigController.class.getResource(Constants.RECORD_INFO));
            RecordInfoPaneController recordInfoPaneController = loader.getController();

            Ibutton ibutton = entry.getKey();
            List<Measurement> measurements = entry.getValue();

            String model = ibutton.getModel();
            String sampleRate = "60 seg";
            String startTime = "";
            String stopTime = "";
            String totalMeasurements = String.valueOf(measurements.size());
            String maxTemp = "";
            String minTemp = "";

            recordInfoPaneController.setData(model, sampleRate, startTime, stopTime, totalMeasurements, maxTemp, minTemp);

            recordInfoTab.setContent(recordInfoPane);
            recordInfoTab.setText(ibutton.getSerial());

            tabPane.getTabs().add(recordInfoTab);
        }
    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override @FXML
    void save() {

        for (List<Measurement> measurements : dataMap.values()) {
            for(Measurement measurement: measurements) {
                measurementService.save(measurement);
            }
        }

        MissionInfoController missionInfoController = VistaNavigator.loadVista(Constants.MISSION_INFO);
        missionInfoController.setData(missionId);
    }

    @Override
    public void translate() {

    }


}
