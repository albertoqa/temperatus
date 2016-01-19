package temperatus.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;
import temperatus.util.Animation;
import temperatus.util.VistaNavigator;

import java.net.URL;
import java.util.*;

/**
 * Created by alberto on 17/1/16.
 */
@Component
public class ConfigurationController implements Initializable {

    @FXML
    private TitledPane titledPane;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private StackPane selectedView;
    @FXML
    private List<Label> menuLabels;

    private static final Map<String, String> labelControllerMap = Collections.unmodifiableMap(
            new HashMap<String, String>() {{
                put("generalView", "/fxml/configuration/General.fxml");
                put("importExport", "/fxml/configuration/ImportExport.fxml");
                put("defaultsView", "/fxml/configuration/Defaults.fxml");
            }});

    private int selectedLabelIndex = -1;    // Index of the label currently selected

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeMenuLabels();
    }

    private void initializeMenuLabels() {
        int index = 0;
        for(Label label: menuLabels) {
            label.setUserData(index);   // index of the label on the list
            label.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    int clickedLabelIndex = (int) label.getUserData();
                    if(selectedLabelIndex != clickedLabelIndex) {
                        colorSelectedLabel(selectedLabelIndex, clickedLabelIndex);
                        selectedLabelIndex = clickedLabelIndex;
                        VistaNavigator.setViewInStackPane(selectedView, labelControllerMap.get(label.getId()));
                    }
                    event.consume();
                }
            });
            index++;
        }
    }

    private void colorSelectedLabel(int selectedLabelIndex, int clickedLabelIndex) {
        menuLabels.get(clickedLabelIndex).setBackground(new Background(new BackgroundFill(Color.CORAL, null, null)));
        if(selectedLabelIndex != -1) {
            menuLabels.get(selectedLabelIndex).setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        }
    }

    @FXML
    private void cancelAction() {
        Animation.fadeInOutClose(titledPane);
    }
}
