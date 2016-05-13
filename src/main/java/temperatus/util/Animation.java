package temperatus.util;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.util.Duration;

/**
 * Animation effects to show/hide nodes
 * <p>
 * Created by alberto on 25/12/15.
 */
public final class Animation {

    private static final double LIFESPAN = 250; // duration
    private static final double LIFESPAN_BLUR = 190; // blur duration
    private static final double INVISIBLE = 0.25;
    private static final double TRANSPARENCY = 0.3; // opacity level
    private static final double VISIBLE = 1.0;
    private static final int MAX = 100;
    private static final int MIN = 0;
    private static final double OPACITY = 0.5;
    private static final double NO_BLUR = 0.0;
    private static final double BLUR = 9.0;     // blur effect level

    /**
     * Fade in transition
     *
     * @param node      node to animate
     * @param invisible state of visibility
     * @param play      play the animation?
     * @return fade transition object
     */
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

    /**
     * Fade out transition
     *
     * @param node      node to animate
     * @param invisible state of visibility
     * @param play      play the animation?
     * @return fade transition object
     */
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

    /**
     * Brings the base stack in and close the given node out
     *
     * @param node node to close
     */
    public static void fadeInOutClose(Node node) {
        FadeTransition fadeIn = Animation.fadeIn(VistaNavigator.baseController.getVistaHolder().getShape(), false, false);
        FadeTransition fadeOut = Animation.fadeOut(node, false, false);
        fadeOut.setOnFinished(actionEvent -> node.getScene().getWindow().hide());
        fadeOut.play();
        fadeIn.play();
    }

    /**
     * Close the given node with an animation
     *
     * @param node node to close
     */
    public static void fadeOutClose(Node node) {
        FadeTransition fadeOut = Animation.fadeOut(node, false, false);
        fadeOut.setOnFinished(actionEvent -> node.getScene().getWindow().hide());
        fadeOut.play();
    }

    /**
     * Take one node out and one node in
     *
     * @param node_out node to take out
     * @param node_in  node to take in
     */
    public static void fadeOutIn(Node node_out, Node node_in) {
        Animation.fadeOut(node_out, false, true);
        Animation.fadeIn(node_in, false, true);
    }

    /**
     * Take a given node in with a transition
     *
     * @param node node to take in
     */
    public static void fadeInTransition(Node node) {
        if (node.getOpacity() < OPACITY) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(LIFESPAN), node);
            fadeIn.setFromValue(MIN);
            fadeIn.setToValue(MAX);
            fadeIn.play();
        }
    }

    /**
     * Take a given node out with a transition
     *
     * @param node node to transition out
     */
    public static void fadeOutTransition(Node node) {
        if (node.getOpacity() > OPACITY) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(LIFESPAN), node);
            fadeOut.setFromValue(MAX);
            fadeOut.setToValue(MIN);
            fadeOut.play();
        }
    }

    /**
     * Apply a gaussian blur to the node given as a parameter
     *
     * @param node node to apply the gaussian blur
     */
    public static void blurOut(Node node) {
        GaussianBlur blur = new GaussianBlur(NO_BLUR);
        node.setEffect(blur);
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(blur.radiusProperty(), BLUR);
        KeyFrame kf = new KeyFrame(Duration.millis(LIFESPAN_BLUR), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    /**
     * Remove the gaussian blur effect from a given node
     *
     * @param node node to remove the gaussian blur from
     */
    public static void blurIn(Node node) {
        GaussianBlur blur = (GaussianBlur) node.getEffect();
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(blur.radiusProperty(), NO_BLUR);
        KeyFrame kf = new KeyFrame(Duration.millis(LIFESPAN_BLUR), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(actionEvent -> node.setEffect(null));
        timeline.play();
    }

}
