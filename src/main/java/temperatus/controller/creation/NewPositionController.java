package temperatus.controller.creation;

import javafx.fxml.Initializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import temperatus.model.service.PositionService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 27/1/16.
 */
@Component
public class NewPositionController extends AbstractCreationController implements Initializable {

    @Autowired PositionService positionService;

    @Override
    void save() {

    }

    @Override
    public void translate() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
    }
}
