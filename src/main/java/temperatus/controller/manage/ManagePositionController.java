package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
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

import java.io.File;
import java.net.URL;
import java.util.List;
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
    private static final String FILE = "file:";
    private static Logger logger = LoggerFactory.getLogger(ManagePositionController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        positions = FXCollections.observableArrayList();
        place.setCellValueFactory(cellData -> cellData.getValue().getPlaceProperty());

        FilteredList<Position> filteredData = new FilteredList<>(positions, p -> true);
        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(position -> newValue == null || newValue.isEmpty() || position.getPlace().toLowerCase().contains(newValue.toLowerCase()));
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, position) -> {
            Animation.fadeInTransition(infoPane);
            if (position != null) {
                nameLabel.setText(position.getPlace());
                if(position.getPicture().equals(DEFAULT_IMAGE)) {
                    imageView.setImage(new Image(DEFAULT_IMAGE));
                } else {
                    try {
                        imageView.setImage(new Image(FILE + position.getPicture()));
                    } catch (Exception ex) {
                        logger.error("Error loading image for position.");
                        imageView.setImage(new Image(DEFAULT_IMAGE));
                    }
                }
            }
        });

        SortedList<Position> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(place);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();

        getAllElements();
    }

    /**
     * Fetch all Positions from database and add it to the table.
     * Use a different thread than the UI thread.
     */
    private void getAllElements() {
        Task<List<Position>> getPositionsTask = new Task<List<Position>>() {
            @Override
            public List<Position> call() throws Exception {
                return positionService.getAll();
            }
        };

        // on task completion add all positions to the table
        getPositionsTask.setOnSucceeded(e -> positions.setAll(getPositionsTask.getValue()));

        // run the task using a thread from the thread pool:
        databaseExecutor.submit(getPositionsTask);
    }

    /**
     * Edit selected position
     */
    @FXML
    private void editPosition() {
        NewPositionController newPositionController = VistaNavigator.openModal(Constants.NEW_POSITION, Constants.EMPTY);
        newPositionController.setPositionForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Create a new position - show newPosition screen
     */
    @FXML
    private void newPosition() {
        VistaNavigator.openModal(Constants.NEW_POSITION, Constants.EMPTY);
    }

    /**
     * Delete selected position. Warn the user that some formulas may stop working.
     */
    @FXML
    private void deletePosition() {
        if (VistaNavigator.confirmationAlert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION_DELETE_POSITION))) {
            Position position = table.getSelectionModel().getSelectedItem();
            positionService.delete(position);
            positions.remove(position);
            String picturePath = position.getPicture();
            if(!picturePath.equals(DEFAULT_IMAGE)) {
                new File(position.getPicture()).delete();
            }
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
        table.setPlaceholder(new Label(language.get(Lang.EMPTY_TABLE_POSITIONS)));
    }

}
