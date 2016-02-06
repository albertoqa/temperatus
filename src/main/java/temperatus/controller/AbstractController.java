package temperatus.controller;

/**
 * Created by alberto on 27/1/16.
 */
public interface AbstractController {

    default void reload(Object object) {
        System.out.println("Nothing to reload...");
    }

    default void transtale() {
        System.out.println("Nothing to translate...");
    }

}
