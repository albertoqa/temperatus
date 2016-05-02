package temperatus.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

/**
 * General constants
 * <p>
 * Created by alberto on 17/1/16.
 */
public class Constants {

    public static final String VERSION = "Version 1.0";
    public static final String WEB = "www.temperatus.com";
    public static final String PROJECT_WEB = "http://albertoqa.github.io/temperatusWeb/";

    public static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static final DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static final int DELAY = 0;    // DeviceDetectorTask delay in seconds
    public static final int PERIOD = 8;   // DeviceDetectorTask run period (s)

    public static final String DEGREE = "º";

    //#########################################################################//
    //* Convenience constants for fxml layouts

    public static final String SPLASH = "/fxml/Welcome.fxml";
    public static final String ACTIVATION = "/fxml/Activation.fxml";
    public static final String BUY_COMPLETE = "/fxml/Activate.fxml";
    public static final String THANKS = "/fxml/Thanks.fxml";

    public static final String BASE = "/fxml/Base.fxml";
    public static final String HOME = "/fxml/Home.fxml";
    public static final String ABOUT = "/fxml/About.fxml";
    public static final String CONFIG = "/fxml/Configuration.fxml";

    public static final String ARCHIVED = "/fxml/Archived.fxml";
    public static final String MISSION_INFO = "/fxml/MissionInfo.fxml";
    public static final String BUTTON_DATA = "/fxml/ButtonData.fxml";
    public static final String TEMPERATURE_LOG = "/fxml/device/TemperatureLog.fxml";
    public static final String MISSION_LINE_CHART = "/fxml/MissionLineChart.fxml";
    public static final String EXPORT_CONFIG = "/fxml/ExportConfiguration.fxml";
    public static final String CONFIG_DEVICE = "/fxml/device/StartDeviceMission.fxml";
    public static final String OUTLIERS = "/fxml/creation/Outliers.fxml";

    public static final String NEW_PROJECT = "/fxml/creation/NewProject.fxml";
    public static final String NEW_MISSION = "/fxml/creation/NewMission.fxml";
    public static final String NEW_GAME = "/fxml/creation/NewGame.fxml";
    public static final String NEW_SUBJECT = "/fxml/creation/NewSubject.fxml";
    public static final String NEW_FORMULA = "/fxml/creation/NewFormula.fxml";
    public static final String NEW_AUTHOR = "/fxml/creation/NewAuthor.fxml";
    public static final String NEW_RECORD = "/fxml/creation/NewRecord.fxml";
    public static final String NEW_POSITION = "/fxml/creation/NewPosition.fxml";
    public static final String NEW_IBUTTON = "/fxml/device/NewIButton.fxml";
    public static final String NEW_CONFIG = "/fxml/creation/NewConfiguration.fxml";
    public static final String RECORD_CONFIG = "/fxml/creation/RecordConfig.fxml";
    public static final String RECORD_INFO = "/fxml/creation/RecordInfo.fxml";

    public static final String CONNECTED = "/fxml/ConnectedDevices.fxml";
    public static final String REAL_TIME_TEMP = "/fxml/device/RealTimeTemp.fxml";
    public static final String DEVICE_MISSION_INFO = "/fxml/device/DeviceMissionInformation.fxml";
    public static final String DEVICE_GENERAL_INFO = "/fxml/device/DeviceGeneralInfo.fxml";
    public static final String MISSION_HELP = "/fxml/DeviceMissionHelp.fxml";

    public static final String MANAGE = "/fxml/Manage.fxml";
    public static final String MANAGE_SUBJECT = "/fxml/manage/ManageSubject.fxml";
    public static final String MANAGE_GAME = "/fxml/manage/ManageGame.fxml";
    public static final String MANAGE_POSITION = "/fxml/manage/ManagePosition.fxml";
    public static final String MANAGE_AUTHOR = "/fxml/manage/ManageAuthor.fxml";
    public static final String MANAGE_FORMULA = "/fxml/manage/ManageFormula.fxml";
    public static final String MANAGE_IBUTTON = "/fxml/manage/ManageIButton.fxml";
    public static final String MANAGE_CONFIGURATIONS = "/fxml/manage/ManageConfiguration.fxml";

    public static final String GAME_INFO = "/fxml/manage/ampliate/GameInfo.fxml";


    //#########################################################################//
    // * Resources addresses

    // Icon images
    public static final String[] ICONS = {"/images/icons/about.png", "/images/icons/archive.png", "/images/icons/author.png",
            "/images/icons/conf.png", "/images/icons/devices.png", "/images/icons/formula.png", "/images/icons/game.png",
            "/images/icons/home.png", "/images/icons/manage.png", "/images/icons/mission.png", "/images/icons/position.png",
            "/images/icons/project.png", "/images/icons/subject.png"};

    public static final int ICON_SIZE = 15;
    public static final int NUMBER_OF_ICONS = 13;

    //#########################################################################//
    // * User preferences

    public static final Preferences prefs = Preferences.userRoot().node("temperatus");

    public static final String ACTIVATED = "activated";

    public static final String FIRST_TIME = "isFirstTime";

    public static final String UNIT = "unit";
    public static final String UNIT_C = "C";
    public static final String UNIT_F = "F";

    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_EN = "en_US";
    public static final String LANGUAGE_SP = "es_ES";
    public static final String PREF_RANGE = "prefRange";
    public static final String DEFAULT_RANGE = "5";
    public static final String WRITE_AS_INDEX = "writeIndex";
    public static final boolean WRITE_INDEX = false;
    public static final String AUTO_SYNC = "autoSync";
    public static final boolean SYNC = false;

    public static final String LANG_EN = "English";
    public static final String LANG_SP = "Spanish";


    private Constants() {
    }

}
