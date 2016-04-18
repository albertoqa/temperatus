package temperatus.util;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Animation effects to show/hide nodes
 * <p>
 * Created by alberto on 25/12/15.
 */
public final class Animation {

    private static final double LIFESPAN = 250; // duration
    private static final double INVISIBLE = 0.25;
    private static final double TRANSPARENCY = 0.3; // opacity level
    private static final double VISIBLE = 1.0;

    private static FadeTransition fadeIn(Node node, Boolean invisible, Boolean play) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(LIFESPAN), node);
        if (invisible) {
            fadeIn.setFromValue(INVISIBLE);
        } else {
            fadeIn.setFromValue(TRANSPARENCY);
        }
        fadeIn.setToValue(VISIBLE);
        if (play) {
            fadeIn.play();
        }
        return fadeIn;
    }

    private static FadeTransition fadeOut(Node node, Boolean invisible, Boolean play) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(LIFESPAN), node);
        fadeOut.setFromValue(VISIBLE);
        if (invisible) {
            fadeOut.setToValue(INVISIBLE);
        } else {
            fadeOut.setToValue(TRANSPARENCY);
        }
        if (play) {
            fadeOut.play();
        }
        return fadeOut;
    }

    static void fadeInOutClose(Node node) {
        FadeTransition fadeIn = Animation.fadeIn(VistaNavigator.baseController.getVistaHolder().getShape(), false, false);
        FadeTransition fadeOut = Animation.fadeOut(node, false, false);
        fadeOut.setOnFinished(actionEvent -> node.getScene().getWindow().hide());
        fadeOut.play();
        fadeIn.play();
    }

    public static void fadeOutIn(Node node_out, Node node_in) {
        Animation.fadeOut(node_out, false, true);
        Animation.fadeIn(node_in, false, true);
    }

    public static void fadeInTransition(Node node) {
        if (node.getOpacity() < 0.5) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(LIFESPAN), node);
            fadeOut.setFromValue(0);
            fadeOut.setToValue(100);
            fadeOut.play();
        }
    }

    public static void fadeOutTransition(Node node) {
        if (node.getOpacity() > 0.5) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(LIFESPAN), node);
            fadeOut.setFromValue(100);
            fadeOut.setToValue(0);
            fadeOut.play();
        }
    }

}
