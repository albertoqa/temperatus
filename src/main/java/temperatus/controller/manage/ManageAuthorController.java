package temperatus.controller.manage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Author;
import temperatus.model.service.AuthorService;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageAuthorController implements Initializable, AbstractController {

    @FXML private TableView<Author> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    private TableColumn<Author, String> name = new TableColumn<>();

    private ObservableList<Author> authors;

    @Autowired AuthorService authorService;

    static Logger logger = LoggerFactory.getLogger(ManageAuthorController.class.getName());


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        authors = FXCollections.observableArrayList();
        addAllAuthors();

        name.setText("Name");
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());

        FilteredList<Author> filteredData = new FilteredList<>(authors, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(subject -> {
                // If filter text is empty, display all authors.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (subject.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, author) -> {
            Animation.fadeInTransition(infoPane);

            if(author != null) {

            }
        });

        SortedList<Author> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(name);
        table.setItems(sortedData);

    }

    private void addAllAuthors() { //TODO
        authors.addAll(authorService.getAll());
    }

    @FXML
    private void showCompleteInfo() {

    }

    @FXML
    private void newAuthor() {

    }

    @FXML
    private void deleteAuthor() {

    }

    @Override
    public void reload(Object object) {
        if(object instanceof Author) {
            authors.add((Author) object);
            table.getSelectionModel().select((Author) object);
        }
    }

    @Override
    public void translate() {

    }

}
