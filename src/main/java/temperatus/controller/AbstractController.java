package temperatus.controller;

/**
 * Created by alberto on 27/1/16.
 */
public interface AbstractController {

    default void reload() {
        System.out.println("Nothing to reload...");
    }

}
