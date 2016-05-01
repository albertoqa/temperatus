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
import temperatus.lang.Lang;
import temperatus.model.pojo.Position;
import temperatus.model.service.PositionService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Allow the user to search, edit, create and delete positions
 * <p>
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManagePositionController implements Initializable, AbstractController {

    @FXML private TableView<Position> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private Label nameLabel;
    @FXML private ImageView imageView;

    private TableColumn<Position, String> place = new TableColumn<>();

    private ObservableList<Position> positions;

    @Autowired PositionService positionService;

    private static final String DEFAULT_IMAGE = "/images/noimage.jpg";  // Set the default image to show -> no image picture
    private static Logger logger = LoggerFactory.getLogger(ManagePositionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        positions = FXCollections.observableArrayList(positionService.getAll());
        place.setCellValueFactory(cellData -> cellData.getValue().getPlaceProperty());

        FilteredList<Position> filteredData = new FilteredList<>(positions, p -> true);
        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(position -> newValue == null || newValue.isEmpty() || position.getPlace().toLowerCase().contains(newValue.toLowerCase()));
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, position) -> {
            Animation.fadeInTransition(infoPane);
            if (position != null) {
                nameLabel.setText(position.getPlace());
                try {
                    imageView.setImage(new Image(position.getPicture()));
                } catch (Exception ex) {
                    imageView.setImage(new Image(DEFAULT_IMAGE));
                }
            }
        });

        SortedList<Position> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(place);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Edit selected position
     */
    @FXML
    private void editPosition() {
        NewPositionController newPositionController = VistaNavigator.openModal(Constants.NEW_POSITION, language.get(Lang.NEW_POSITION));
        newPositionController.setPositionForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Create a new position - show newPosition screen
     */
    @FXML
    private void newPosition() {
        VistaNavigator.openModal(Constants.NEW_POSITION, language.get(Lang.NEW_POSITION));
    }

    /**
     * Delete selected position. Warn the user that some formulas may stop working.
     */
    @FXML
    private void deletePosition() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION_DELETE_POSITION));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Position position = table.getSelectionModel().getSelectedItem();
            positionService.delete(position);
            positions.remove(position);
            logger.info("Deleted position... " + position);
        }
    }

    /**
     * Reload edited/created position
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Position) {
            if (!positions.contains(object)) {
                positions.add((Position) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Position) object);
        }
    }

    @Override
    public void translate() {
        filterInput.setPromptText(language.get(Lang.FILTER));
        place.setText(language.get(Lang.NAME_COLUMN));
        editButton.setText(language.get(Lang.EDIT));
        deleteButton.setText(language.get(Lang.DELETE));
        newElementButton.setText(language.get(Lang.NEW_POSITION));
    }

}
