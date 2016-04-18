package temperatus.controller.manage.ampliate;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import temperatus.controller.AbstractController;
import temperatus.model.pojo.Game;
import temperatus.model.pojo.Image;
import temperatus.util.VistaNavigator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alberto on 29/2/16.
 */
@Controller
@Scope("prototype")
public class GameInfoController implements AbstractController {

    @FXML private Button backButton;

    @FXML private ImageView imFront;
    @FXML private ImageView imLat;
    @FXML private ImageView imBack;

    private Game game;

    public void setGame(Game game) {
        this.game = game;
        loadData();
    }

    private void loadData() {
        List<Image> imagesPaths = new ArrayList<>(game.getImages());

        for(Image image: imagesPaths) {
            javafx.scene.image.Image im = new javafx.scene.image.Image("file:" + image.getPath());

            if(image.getPath().contains("0")) {
                imFront.setImage(im);
            } else if (image.getPath().contains("1")) {
                imLat.setImage(im);
            } else {
                imBack.setImage(im);
            }
        }



    }

    @FXML
    private void back() {
        VistaNavigator.popViewFromStack();
    }

    @Override
    public void translate() {

    }

}
