package temperatus.util;

import java.util.prefs.Preferences;

/**
 * Created by alberto on 17/1/16.
 */
public class Constants {

    //#########################################################################//
    //* Convenience constants for fxml layouts managed by the navigator.

    public static final String WELCOME = "/fxml/Welcome.fxml";

    public static final String BASE = "/fxml/Base.fxml";
    public static final String HOME = "/fxml/Home.fxml";

    public static final String CONFIG = "/fxml/Configuration.fxml";
    public static final String CONFIG_GENERAL = "/fxml/configuration/General.fxml";
    public static final String CONFIG_IMPORTEXPORT = "/fxml/configuration/ImportExport.fxml";
    public static final String CONFIG_DEFAULTS = "/fxml/configuration/Defaults.fxml";

    public static final String ARCHIVED = "/fxml/Archived.fxml";
    public static final String MISSION_INFO = "/fxml/MissionInfo.fxml";

    public static final String NEW_PROJECT = "/fxml/NewProject.fxml";
    public static final String NEW_MISSION = "/fxml/mission/NewMission.fxml";
    public static final String NEW_GAME = "/fxml/mission/NewGame.fxml";
    public static final String NEW_SUBJECT = "/fxml/NewSubject.fxml";
    public static final String NEW_RECORD = "/fxml/mission/NewRecord.fxml";

    public static final String CONNECTED = "/fxml/ConnectedDevices.fxml";

    //#########################################################################//

    public static Preferences prefs = Preferences.userRoot().node("temperatus");

    public static final String LANGUAGE = "language";
    public static final String DEFAULT_LANGUAGE = "en_US";

    public static final double MIN_HEIGHT = 800.0;
    public static final double MIN_WIDTH = 1000.0;

    //#########################################################################//
    // * Constants for language names

    public static final String RIGHTS = "rights";


}
