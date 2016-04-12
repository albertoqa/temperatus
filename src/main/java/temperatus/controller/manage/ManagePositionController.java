package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewPositionController;
import temperatus.model.pojo.Position;
import temperatus.model.service.PositionService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManagePositionController implements Initializable, AbstractController {

    @FXML private TableView<Position> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    @FXML private Label positionName;
    @FXML private ImageView imageView;

    private TableColumn<Position, String> place = new TableColumn<>();

    private ObservableList<Position> positions;

    @Autowired PositionService positionService;

    static Logger logger = LoggerFactory.getLogger(ManagePositionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        positions = FXCollections.observableArrayList();
        addAllPositions();

        place.setText("Name");
        place.setCellValueFactory(cellData -> cellData.getValue().getPlaceProperty());

        FilteredList<Position> filteredData = new FilteredList<>(positions, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(position -> {
                // If filter text is empty, display all authors.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (position.getPlace().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, position) -> {
            Animation.fadeInTransition(infoPane);

            if(position != null) {
                positionName.setText(position.getPlace());
                try {
                    imageView.setImage(new Image(position.getPicture()));
                } catch (Exception ex) {
                    // TODO show no image
                }
            }
        });

        SortedList<Position> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(place);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    private void addAllPositions() {
        positions.addAll(positionService.getAll());
    }

    @FXML
    private void editPosition() {
        NewPositionController newPositionController = VistaNavigator.openModal(Constants.NEW_POSITION, language.get(Constants.NEWPOSITION));
        newPositionController.setPositionForUpdate(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void newPosition() {
        VistaNavigator.openModal(Constants.NEW_POSITION, language.get(Constants.NEWPOSITION));
    }

    @FXML
    private void deletePosition() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Position position = table.getSelectionModel().getSelectedItem();
            positionService.delete(position);
            positions.remove(position);
        }
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Position) {
            if(!positions.contains((Position) object)) {
                positions.add((Position) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().select((Position) object);
        }
    }

    @Override
    public void translate() {

    }

}
