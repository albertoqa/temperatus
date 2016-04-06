package temperatus.controller.archived;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.model.pojo.Measurement;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alberto on 5/4/16.
 */
@Controller
@Scope("prototype")
public class ButtonDataController implements Initializable {

    @FXML private TableView<Measurement> tableView;
    @FXML private StackPane stackPane;

    private AnchorPane parentNode;

    private TableColumn<Measurement, String> date = new TableColumn<>();
    private TableColumn<Measurement, String> unit = new TableColumn<>();
    private TableColumn<Measurement, String> value = new TableColumn<>();

    private ObservableList<Measurement> measurements;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        measurements = FXCollections.observableArrayList();

        date.setText("Date");
        date.setCellValueFactory(cellData -> cellData.getValue().getDateProperty());
        unit.setText("Unit");
        unit.setCellValueFactory(cellData -> cellData.getValue().getUnitProperty());
        value.setText("Value");
        value.setCellValueFactory(cellData -> cellData.getValue().getDataProperty());

        tableView.getColumns().addAll(date, unit, value);
        tableView.setItems(measurements);
    }

    public void setData(List<Measurement> measurements) {
        this.measurements.addAll(measurements);
    }

    public void setParentNode(AnchorPane parentNode) {
        this.parentNode = parentNode;
    }

    @FXML
    private void back() {
//        Animation.fadeInOutClose(stackPane);
        VistaNavigator.closeModal(stackPane, parentNode);
    }


}
