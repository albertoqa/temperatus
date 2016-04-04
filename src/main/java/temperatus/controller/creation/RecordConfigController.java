package temperatus.controller.creation;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.analysis.pojo.GeneralData;
import temperatus.analysis.pojo.ValidatedData;
import temperatus.controller.archived.MissionInfoController;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.types.ListViewItem;
import temperatus.model.service.FormulaService;
import temperatus.model.service.MeasurementService;
import temperatus.model.service.MissionService;
import temperatus.util.Constants;
import temperatus.util.DateStringConverter;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * Created by alberto on 7/2/16.
 */
@Controller
@Scope("prototype")
public class RecordConfigController extends AbstractCreationController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Tab generalTab;
    @FXML private Button addFormulaButton;
    @FXML private ListView<ListViewItem> listViewFormulas;

    @FXML private RangeSlider rangeSlider;
    @FXML private TextField initTime;
    @FXML private TextField endTime;

    @FXML private Label modelLabel;
    @FXML private Label rateLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label avgMeasurementsLabel;
    @FXML private Label maxTempLabel;
    @FXML private Label minTempLabel;
    @FXML private Label avgTempLabel;

    @Autowired MeasurementService measurementService;
    @Autowired FormulaService formulaService;
    @Autowired MissionService missionService;

    static Logger logger = LoggerFactory.getLogger(NewProjectController.class.getName());

    private List<ValidatedData> data;
    private GeneralData generalData;
    private Mission mission;  // Parent mission

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    /**
     * All iButtons must be already saved to DB, measurements will be saved by this controller
     */
    public void setData(List<ValidatedData> data, GeneralData generalData) {
        this.data = data;
        this.generalData = generalData;
        loadFormulas();
        loadData();
        loadTimeRange();
    }

    /**
     * Create a new tab for each iButton with all its info
     */
    private void loadData() {
        loadGeneralData();

        for (ValidatedData validatedData : data) {

            Tab recordInfoTab = new Tab();
            StackPane stackPane = new StackPane();

            // Load a new pane for the tab
            Node recordInfoPane = VistaNavigator.loader.load(RecordConfigController.class.getResource(Constants.RECORD_INFO));
            RecordInfoPaneController recordInfoPaneController = VistaNavigator.loader.getController();

            String model = validatedData.getDeviceModel();
            String serial = validatedData.getDeviceSerial();
            String alias = validatedData.getIbutton().getAlias();
            String sampleRate = validatedData.getSampleRate();
            String startDate = validatedData.getStartDate().toString();
            String endDate = validatedData.getFinishDate().toString();
            String totalMeasurements = String.valueOf(validatedData.getMeasurements().size());
            String position = validatedData.getPosition().getPlace();

            // Store information in loaded pane
            recordInfoPaneController.setData(model, serial, alias, sampleRate, startDate, endDate, totalMeasurements, position);

            stackPane.getChildren().setAll(recordInfoPane);
            recordInfoTab.setContent(stackPane);
            recordInfoTab.setText(serial);

            // Add new tab
            tabPane.getTabs().add(recordInfoTab);
        }

    }

    /**
     * Set rangeSlider and textInputs to init and end date
     */
    private void loadTimeRange() {
        // TODO remove mock data
        rangeSlider.setMax(generalData.getEndDate().getTime() + 50*60000);
        rangeSlider.setMin(generalData.getStartDate().getTime());
        rangeSlider.setHighValue(generalData.getEndDate().getTime() + 47*60000);
        rangeSlider.setLowValue(generalData.getStartDate().getTime() + 3*60000);
        rangeSlider.setLabelFormatter(new DateStringConverter(true));
        rangeSlider.setShowTickMarks(true);
        rangeSlider.setShowTickLabels(true);

        // TODO set this according to the amount of time
        rangeSlider.setMajorTickUnit(300000);
        rangeSlider.setBlockIncrement(60000);

        Bindings.bindBidirectional(initTime.textProperty(), rangeSlider.lowValueProperty(), new DateStringConverter(false));
        Bindings.bindBidirectional(endTime.textProperty(), rangeSlider.highValueProperty(), new DateStringConverter(false));
    }

    /**
     * Show general data on screen (labels)
     */
    private void loadGeneralData() {
        modelLabel.setText(generalData.getModels());
        rateLabel.setText(generalData.getRate());
        startDateLabel.setText(generalData.getStartDate().toString());
        endDateLabel.setText(generalData.getEndDate().toString());
        avgMeasurementsLabel.setText(String.valueOf(generalData.getMeasurementsPerButton()));
        maxTempLabel.setText(String.valueOf(generalData.getMaxTemp()));
        minTempLabel.setText(String.valueOf(generalData.getMinTemp()));
        avgTempLabel.setText(String.valueOf(generalData.getAvgTemp()));
    }

    /**
     * Load all formulas from DB
     * Pre-select default formulas for this game
     */
    private void loadFormulas() {
        List<Formula> formulas = formulaService.getAll();
        Set<Formula> defaultFormulas = mission.getGame().getFormulas();

        List<ListViewItem> items = new ArrayList<>();
        for (Formula formula : formulas) {
            boolean checked = defaultFormulas.contains(formula);
            items.add(new ListViewItem(formula.getName(), checked));
        }

        listViewFormulas.getItems().addAll(items);
        listViewFormulas.setCellFactory(CheckBoxListCell.forListView(new Callback<ListViewItem, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(ListViewItem item) {
                return item.onProperty();
            }
        }));
    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    @FXML
    void save() {

        Date startDate = null;
        Date endDate = null;

        try {
            startDate = Constants.dateTimeFormat.parse(initTime.getText());
            endDate = Constants.dateTimeFormat.parse(endTime.getText());
        } catch (ParseException e) {
            // TODO exception y break
        }

        // Save measurements + check if is in the range
        for (ValidatedData validatedData: data) {
            for (Measurement measurement : validatedData.getMeasurements()) {
                if(measurement.getDate().after(startDate) || measurement.getDate().before(endDate)) {
                    measurementService.save(measurement);
                }
            }
        }

        Set<Formula> selectedFormulas = new HashSet<>();
        for(ListViewItem item: listViewFormulas.getItems()) {
            if(item.isOn()) {
                selectedFormulas.add(formulaService.getByName(item.getName()));
            }
        }

        mission.setFormulas(selectedFormulas);
        missionService.saveOrUpdate(mission);

        MissionInfoController missionInfoController = VistaNavigator.loadVista(Constants.MISSION_INFO);
        // missionInfoController.setData(missionId);
    }

    @Override
    public void translate() {

    }

}