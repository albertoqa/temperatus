package temperatus.controller;

/**
 * Abstract class for all controllers
 *
 * Created by alberto on 27/1/16.
 */
public interface AbstractController {

    /**
     * Reload a given object
     * @param object
     */
    default void reload(Object object) {
        System.out.println("Nothing to reload...");
    }

    /**
     * Translate all labels, buttons, titles for a view
     */
    void translate();

}
