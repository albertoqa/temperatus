package temperatus.util;

import temperatus.model.pojo.Author;

/**
 * Store the user currently using the application
 * <p>
 * Created by alberto on 3/5/16.
 */
public class User {

    private static Author user;     // person currently using ("logged") the application

    public static Author getUser() {
        return user;
    }

    public static void setUser(Author user) {
        User.user = user;
        VistaNavigator.baseController.setUserName(user.getName());
    }

    public static String getUserName() {
        if (user != null) {
            return user.getName();
        } else {
            return "";
        }
    }

}
