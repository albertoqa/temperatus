package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.pojo.Mission;
import temperatus.model.service.MissionService;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 30/1/16.
 */
@Component
public class MissionInfoController implements Initializable {

    @Autowired MissionService missionService;

    private Mission mission;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setMission(int missionId) {
        mission = missionService.getById(missionId);
        //TODO throw exception if mission == null
    }

    @FXML
    private void back() {
        VistaNavigator.loadVista(Constants.ARCHIVED);
    }
}
