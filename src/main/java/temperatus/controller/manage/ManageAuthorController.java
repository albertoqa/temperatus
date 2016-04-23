package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.controller.creation.NewAuthorController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Author;
import temperatus.model.pojo.Mission;
import temperatus.model.service.AuthorService;
import temperatus.util.Animation;
import temperatus.util.Constants;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Allow the user to search, edit, delete and create authors
 * <p>
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageAuthorController implements Initializable, AbstractController {

    @FXML private TableView<Author> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    @FXML private Button editAuthorButton;
    @FXML private Button deleteAuthorButton;

    @FXML private Label projectsLabel;
    @FXML private Label nameInfo;
    @FXML private Label projectsInfo;

    private TableColumn<Author, String> name = new TableColumn<>();

    private ObservableList<Author> authors;

    @Autowired AuthorService authorService;

    private static final String NEW_LINE = "\n";
    private static Logger logger = LoggerFactory.getLogger(ManageAuthorController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        authors = FXCollections.observableArrayList(authorService.getAll());
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());

        // allow to filter the table
        FilteredList<Author> filteredData = new FilteredList<>(authors, p -> true);
        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(subject -> newValue == null || newValue.isEmpty() || subject.getName().toLowerCase().contains(newValue.toLowerCase()));
        });

        // show pane with info when author selected
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, author) -> {
            Animation.fadeInTransition(infoPane);
            if (author != null) {
                nameInfo.setText(author.getName().toUpperCase());
                String projects = "";
                for(Mission mission: author.getMissions()) {
                    if(!projects.contains(mission.getProject().getName())) {
                        projects = projects + "- " + mission.getProject().getName() + NEW_LINE;
                    }
                }
                projectsInfo.setText(projects);
            }
        });

        // allow to sort the table
        SortedList<Author> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(name);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Show the new author screen with the info of the selected author pre-loaded
     */
    @FXML
    private void editAuthor() {
        NewAuthorController newAuthorController = VistaNavigator.openModal(Constants.NEW_AUTHOR, language.get(Lang.NEWAUTHOR));
        newAuthorController.setAuthorForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Show the new author screen
     */
    @FXML
    private void newAuthor() {
        VistaNavigator.openModal(Constants.NEW_AUTHOR, language.get(Lang.NEWAUTHOR));
    }

    /**
     * Delete the selected author from the database and from the table
     */
    @FXML
    private void deleteAuthor() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Author author = table.getSelectionModel().getSelectedItem();
            authorService.delete(author);
            authors.remove(author);

            logger.debug("Author removed: " + author);
        }
    }

    /**
     * Reload Author on edit/create
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        if (object instanceof Author) {
            if (!authors.contains(object)) {
                authors.add((Author) object);
            }
            table.getColumns().get(0).setVisible(false);    // needed to refresh the table
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Author) object);
            logger.debug("Author reloaded: " + object);
        }
    }

    @Override
    public void translate() {
        name.setText(language.get(Lang.NAME_COLUMN));
        projectsLabel.setText(language.get(Lang.PARTICIPATE_IN_PROJECTS));
        editAuthorButton.setText(language.get(Lang.EDIT));
        newElementButton.setText(language.get(Lang.NEWAUTHOR));
        deleteAuthorButton.setText(language.get(Lang.DELETE));
    }

}
