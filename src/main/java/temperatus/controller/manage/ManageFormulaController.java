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
import temperatus.controller.creation.NewFormulaController;
import temperatus.lang.Lang;
import temperatus.model.pojo.Formula;
import temperatus.model.service.FormulaService;
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
public class ManageFormulaController implements Initializable, AbstractController {

    @FXML private TableView<Formula> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;
    @FXML private Button newElementButton;

    private TableColumn<Formula, String> name = new TableColumn<>();
    private TableColumn<Formula, String> reference = new TableColumn<>();
    private TableColumn<Formula, String> operation = new TableColumn<>();

    private ObservableList<Formula> formulas;

    @Autowired FormulaService formulaService;

    static Logger logger = LoggerFactory.getLogger(ManageFormulaController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        formulas = FXCollections.observableArrayList();
        addAllFormulas();

        name.setText("Name");
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        reference.setText("Reference");
        reference.setCellValueFactory(cellData -> cellData.getValue().getReferenceProperty());
        operation.setText("Operation");
        operation.setCellValueFactory(cellData -> cellData.getValue().getOperationProperty());

        FilteredList<Formula> filteredData = new FilteredList<>(formulas, p -> true);

        filterInput.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(formula -> {
                // If filter text is empty, display all authors.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (formula.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if(formula.getReference().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if(formula.getOperation().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, formula) -> {
            Animation.fadeInTransition(infoPane);

            if(formula != null) {
                // TODO
            }

        });

        SortedList<Formula> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(name, reference, operation);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    private void addAllFormulas() {
        formulas.addAll(formulaService.getAll());
    }

    @FXML
    private void editFormula() {
        NewFormulaController newFormulaController = VistaNavigator.openModal(Constants.NEW_FORMULA, language.get(Lang.NEWFORMULA));
        newFormulaController.setFormulaForUpdate(table.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void newFormula() {
        VistaNavigator.openModal(Constants.NEW_FORMULA, language.get(Lang.NEWFORMULA));
    }

    @FXML
    private void deleteFormula() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Formula formula = table.getSelectionModel().getSelectedItem();
            formulaService.delete(formula);
            formulas.remove(formula);
        }
    }

    @Override
    public void reload(Object object) {
        if(object instanceof Formula) {
            if(!formulas.contains((Formula) object)) {
                formulas.add((Formula) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().select((Formula) object);
        }
    }

    @Override
    public void translate() {

    }

}
