package temperatus.controller.manage.ampliate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Game;
import temperatus.util.VistaNavigator;

/**
 * Created by alberto on 29/2/16.
 */
@Controller
@Scope("prototype")
public class GameInfoController implements AbstractController {

    @FXML private Button backButton;

    private Game game;

    public void setGame(Game game) {
        this.game = game;
        loadData();
    }

    private void loadData() {

    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    public void translate() {

    }

}
