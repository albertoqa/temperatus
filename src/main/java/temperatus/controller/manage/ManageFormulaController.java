package temperatus.controller.manage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Formula;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageFormulaController implements Initializable, AbstractController {

    @FXML private TableView<Formula> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void reload(Object object) {

    }

    @Override
    public void translate() {

    }

}
