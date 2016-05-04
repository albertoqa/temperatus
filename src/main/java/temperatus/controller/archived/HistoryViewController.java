package temperatus.controller.archived;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.lang.Lang;
import temperatus.util.ReverseLineInputStream;
import temperatus.util.User;
import temperatus.util.VistaNavigator;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Show a window with the history of use of the application. Also allow to clean the history.
 * <p>
 * Created by alberto on 3/5/16.
 */
@Controller
public class HistoryViewController implements Initializable, AbstractController {

    @FXML private Label headerLabel;
    @FXML private Button backButton;
    @FXML private Button cleanButton;
    @FXML private TextArea historyContent;

    private static final String NEW_LINE = "\n";
    private static final String EMPTY = "";
    private static final String HISTORY_FILE = "./history.txt";

    private static Logger logger = LoggerFactory.getLogger(HistoryViewController.class.getName());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        translate();
        readFileContent();
    }

    /**
     * Read history file and add its content to the textArea
     */
    private void readFileContent() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(new File(HISTORY_FILE))));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                historyContent.appendText(line + NEW_LINE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset the history log
     */
    @FXML
    private void resetLog() {
        logger.info("Resetting history log");
        try {
            new PrintWriter(HISTORY_FILE).close();
            historyContent.setText(EMPTY);
            history.info(User.getUserName() + " " + language.get(Lang.CLEAN_HISTORY));
            readFileContent();
        } catch (FileNotFoundException e) {
            logger.error("History file not found!");
        }
    }

    /**
     * Go back to previous view
     */
    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    public void translate() {
        headerLabel.setText(language.get(Lang.HISTORY));
        backButton.setText(language.get(Lang.BACK_BUTTON));
        cleanButton.setText(language.get(Lang.CLEAN_HISTORY_BUTTON));
    }

}
