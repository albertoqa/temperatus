package temperatus.util;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;

/**
 * Created by alberto on 25/12/15.
 */
public final class Animation {

    private Animation(){ }

    private static final double DURACAO = 250;
    private static final double INVISIVEL = 0.25;
    private static final double TRANSPARENTE = 0.3;
    private static final double VISIVEL = 1.0;

    private static FadeTransition fadeIn(Node node, Boolean invisivel, Boolean play){
        FadeTransition fadeIn = new FadeTransition(Duration.millis(DURACAO), node);
        if(invisivel){
            fadeIn.setFromValue(INVISIVEL);
        }else{
            fadeIn.setFromValue(TRANSPARENTE);
        }
        fadeIn.setToValue(VISIVEL);
        if(play){
            fadeIn.play();
        }
        return fadeIn;
    }

    private static FadeTransition fadeOut(Node node, Boolean invisivel, Boolean play){
        FadeTransition fadeOut = new FadeTransition(Duration.millis(DURACAO), node);
        fadeOut.setFromValue(VISIVEL);
        if(invisivel){
            fadeOut.setToValue(INVISIVEL);
        }else{
            fadeOut.setToValue(TRANSPARENTE);
        }
        if(play){
            fadeOut.play();
        }
        return fadeOut;
    }

    private static FadeTransition fadeOutReplace(StackPane painel, Node node){
        FadeTransition fadeOut = Animation.fadeOut(painel, false, false);
        fadeOut.setOnFinished(actionEvent -> painel.getChildren().setAll(node));
        return fadeOut;
    }

    private static void fadeTransicao(FadeTransition fadeOut, FadeTransition fadeIn){
        SequentialTransition transicao = new SequentialTransition(fadeOut, fadeIn);
        transicao.play();
    }

    public static void fadeInOutClose(Node node){
        FadeTransition fadeIn = Animation.fadeIn(VistaNavigator.baseController.getVistaHolder().getShape(), false, false);
        FadeTransition fadeOut = Animation.fadeOut(node, false, false);
        fadeOut.setOnFinished(actionEvent -> node.getScene().getWindow().hide());
        fadeOut.play();
        fadeIn.play();
    }

    public static void fadeOutClose(Node node){
        FadeTransition fadeOut = Animation.fadeOut(node, false, false);
        fadeOut.setOnFinished(actionEvent -> node.getScene().getWindow().hide());
        fadeOut.play();
    }

    public static void fadeOutIn(Node node){
        Animation.fadeOut(node, false, true);
        Animation.fadeIn(node, false, true);
    }

    public static void fadeOutIn(Node node_out, Node node_in){
        Animation.fadeOut(node_out, false, true);
        Animation.fadeIn(node_in, false, true);
    }

    public static void fadeOutInReplace(StackPane painel, Node node){
        FadeTransition fadeOut = Animation.fadeOutReplace(painel, node);
        FadeTransition fadeIn = Animation.fadeIn(painel, false, false);
        Animation.fadeTransicao(fadeOut, fadeIn);
    }

    public static void fadeInInvisivel(Node node_foco, Node node_formulario){
        node_foco.requestFocus();
        Animation.fadeIn(node_formulario, true, true);
        node_formulario.getScene().getWindow().setOpacity(1);
    }

    public static void fadeOutInvisivel(Node node_foco, Node node_formulario){
        Animation.fadeOut(node_formulario, true, true);
        node_formulario.getScene().getWindow().setOpacity(0);
        node_foco.requestFocus();
    }

    public static void fadeOutMultiplo(List<Node> nodes){
        nodes.stream().forEach(node -> {
            if(node.getOpacity()>INVISIVEL){
                Animation.fadeOut(node, true, true);
            }
        });
    }

    public static void fadeInMultiplo(List<Node> nodes){
        nodes.stream().forEach(node -> {
            if(node.getOpacity()<VISIVEL){
                Animation.fadeIn(node, true, true);
            }
        });
    }



}
