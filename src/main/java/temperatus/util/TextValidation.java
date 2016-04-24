package temperatus.util;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * TextFields Utils to prevent some inputs
 * <p>
 * Created by alberto on 24/4/16.
 */
public class TextValidation {

    /**
     * Validate textField input to only numbers with a max longitude
     *
     * @param max_Length max longitude of the input
     * @return eventHandler
     */
    public static EventHandler<KeyEvent> numeric(final Integer max_Length) {
        return e -> {
            TextField txt_TextField = (TextField) e.getSource();
            if (txt_TextField.getText().length() >= max_Length) {
                e.consume();
            }
            if (e.getCharacter().matches("[0-9.]")) {
                if (txt_TextField.getText().contains(".") && e.getCharacter().matches("[.]")) {
                    e.consume();
                } else if (txt_TextField.getText().length() == 0 && e.getCharacter().matches("[.]")) {
                    e.consume();
                }
            } else {
                e.consume();
            }
        };
    }

    /**
     * Validate textField input to only letters with a max longitude
     *
     * @param max_Length max longitude of the input
     * @return eventHandler
     */
    public EventHandler<KeyEvent> letter(final Integer max_Length) {
        return e -> {
            TextField txt_TextField = (TextField) e.getSource();
            if (txt_TextField.getText().length() >= max_Length) {
                e.consume();
            }
            if (!e.getCharacter().matches("[A-Za-z]")) {
                e.consume();
            }
        };
    }

}
