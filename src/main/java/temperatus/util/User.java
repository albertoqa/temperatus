package temperatus.util;

import temperatus.model.pojo.Author;

/**
 * Store the user currently using the application. No need of password, just for history control.
 * <p>
 * Created by alberto on 3/5/16.
 */
public class User {

    private static Author user;     // person currently using ("logged") the application. Shared by all the application

    /**
     * Get the user currently logged
     * @return user logged
     */
    public static Author getUser() {
        return user;
    }

    /**
     * Set the user currently logged and change the base view user label to set his/her name
     * @param user new user using the application
     */
    public static void setUser(Author user) {
        User.user = user;
        VistaNavigator.baseController.setUserName(user.getName());
    }

    /**
     * Get the name of the user currently logged
     * @return name of the user
     */
    public static String getUserName() {
        if (user != null) {
            return user.getName();
        } else {
            return "";
        }
    }

}
