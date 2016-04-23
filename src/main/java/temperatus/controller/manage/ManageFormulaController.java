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
 * Allow the user to search, edit, delete and create formulas
 *
 * Created by alberto on 15/2/16.
 */
@Controller
@Scope("prototype")
public class ManageFormulaController implements Initializable, AbstractController {

    @FXML private TableView<Formula> table;
    @FXML private TextField filterInput;
    @FXML private AnchorPane infoPane;

    @FXML private Button newElementButton;
    @FXML private Button editFormula;
    @FXML private Button deleteFormula;

    @FXML private Label nameLabel;
    @FXML private Label referenceLabel;
    @FXML private Label operationLabel;
    @FXML private Label operationInfo;
    @FXML private Label referenceInfo;

    private TableColumn<Formula, String> name = new TableColumn<>();
    private TableColumn<Formula, String> reference = new TableColumn<>();
    private TableColumn<Formula, String> operation = new TableColumn<>();

    private ObservableList<Formula> formulas;

    @Autowired FormulaService formulaService;

    private static Logger logger = LoggerFactory.getLogger(ManageFormulaController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VistaNavigator.setController(this);
        translate();

        formulas = FXCollections.observableArrayList(formulaService.getAll());
        name.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        reference.setCellValueFactory(cellData -> cellData.getValue().getReferenceProperty());
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
                } else if (formula.getReference().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (formula.getOperation().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, formula) -> {
            Animation.fadeInTransition(infoPane);
            if (formula != null) {
                nameLabel.setText(formula.getName().toUpperCase());
                referenceInfo.setText(formula.getReference());
                operationInfo.setText(formula.getOperation());
            }
        });

        SortedList<Formula> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());

        table.getColumns().addAll(name, reference, operation);
        table.setItems(sortedData);
        table.getSelectionModel().clearSelection();
    }

    /**
     * Show new formula controller with the pre-loaded data
     */
    @FXML
    private void editFormula() {
        NewFormulaController newFormulaController = VistaNavigator.openModal(Constants.NEW_FORMULA, language.get(Lang.NEWFORMULA));
        newFormulaController.setFormulaForUpdate(table.getSelectionModel().getSelectedItem());
    }

    /**
     * Show the modal new formula screen
     */
    @FXML
    private void newFormula() {
        VistaNavigator.openModal(Constants.NEW_FORMULA, language.get(Lang.NEWFORMULA));
    }

    /**
     * Delete the selected formula from the database and the table
     */
    @FXML
    private void deleteFormula() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, language.get(Lang.CONFIRMATION));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Formula formula = table.getSelectionModel().getSelectedItem();
            formulaService.delete(formula);
            formulas.remove(formula);
            logger.info("Deleted formula... " + formula);
        }
    }

    /**
     * Reload a new/edited formula
     *
     * @param object object to reload
     */
    @Override
    public void reload(Object object) {
        logger.info("Reloading formula?... ");

        if (object instanceof Formula) {
            if (!formulas.contains(object)) {
                formulas.add((Formula) object);
            }
            table.getColumns().get(0).setVisible(false);
            table.getColumns().get(0).setVisible(true);
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select((Formula) object);
        }
    }

    @Override
    public void translate() {
        name.setText(language.get(Lang.NAME_COLUMN));
        reference.setText(language.get(Lang.REFERENCE_COLUMN));
        operation.setText(language.get(Lang.OPERATION_COLUMN));
        editFormula.setText(language.get(Lang.EDIT));
        newElementButton.setText(language.get(Lang.NEWFORMULA));
        deleteFormula.setText(language.get(Lang.DELETE));
        referenceLabel.setText(language.get(Lang.REFERENCELABEL));
        operationLabel.setText(language.get(Lang.OPERATIONLABEL));
    }
}
