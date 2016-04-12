package temperatus.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

/**
 * Created by alberto on 17/1/16.
 */
public class Constants {

    public static final String VERSION = "1.0";

    public static int DELAY = 0;
    public static int PERIOD = 8;   // DeviceDetectorTask run period (s)

    //#########################################################################//
    //* Convenience constants for fxml layouts

    public static final String SPLASH = "/fxml/Welcome.fxml";

    public static final String BASE = "/fxml/Base.fxml";
    public static final String HOME = "/fxml/Home.fxml";
    public static final String ABOUT = "/fxml/About.fxml";

    public static final String CONFIG = "/fxml/Configuration.fxml";
    public static final String SUBJECT_CONFIG = "/fxml/Configuration.fxml";

    public static final String CONFIG_GENERAL = "/fxml/configuration/General.fxml";
    public static final String CONFIG_IMPORTEXPORT = "/fxml/configuration/ImportExport.fxml";
    public static final String CONFIG_DEFAULTS = "/fxml/configuration/Defaults.fxml";

    public static final String ARCHIVED = "/fxml/Archived.fxml";
    public static final String MISSION_INFO = "/fxml/MissionInfo.fxml";
    public static final String BUTTON_DATA = "/fxml/ButtonData.fxml";
    public static final String MISSION_LINE_CHART = "/fxml/MissionLineChart.fxml";
    public static final String EXPORT_CONFIG = "/fxml/ExportConfiguration.fxml";

    public static final String NEW_PROJECT = "/fxml/creation/NewProject.fxml";
    public static final String NEW_MISSION = "/fxml/creation/NewMission.fxml";
    public static final String NEW_GAME = "/fxml/creation/NewGame.fxml";
    public static final String NEW_SUBJECT = "/fxml/creation/NewSubject.fxml";
    public static final String NEW_FORMULA = "/fxml/creation/NewFormula.fxml";
    public static final String NEW_AUTHOR = "/fxml/creation/NewAuthor.fxml";
    public static final String NEW_RECORD = "/fxml/creation/NewRecord.fxml";
    public static final String NEW_POSITION = "/fxml/creation/NewPosition.fxml";
    public static final String NEW_IBUTTON = "/fxml/device/NewIButton.fxml";
    public static final String RECORD_CONFIG = "/fxml/creation/RecordConfig.fxml";
    public static final String RECORD_INFO = "/fxml/creation/RecordInfo.fxml";

    public static final String CONNECTED = "/fxml/ConnectedDevices.fxml";

    public static final String MANAGE = "/fxml/Manage.fxml";
    public static final String MANAGE_SUBJECT = "/fxml/manage/ManageSubject.fxml";
    public static final String MANAGE_GAME = "/fxml/manage/ManageGame.fxml";
    public static final String MANAGE_POSITION = "/fxml/manage/ManagePosition.fxml";
    public static final String MANAGE_AUTHOR = "/fxml/manage/ManageAuthor.fxml";
    public static final String MANAGE_FORMULA = "/fxml/manage/ManageFormula.fxml";
    public static final String MANAGE_IBUTTON = "/fxml/manage/ManageIButton.fxml";

    public static final String GAME_INFO = "/fxml/manage/ampliate/GameInfo.fxml";

    //#########################################################################//

    public static Preferences prefs = Preferences.userRoot().node("temperatus");

    public static final String FIRST_TIME = "isFirstTime";

    public static final String UNIT = "unit";
    public static final String UNIT_C = "C";
    public static final String UNIT_F = "F";

    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_EN = "en_US";
    public static final String LANGUAGE_SP = "es_ES";
    public static final String PREFRANGE = "prefRange";
    public static final String DEFAULT_RANGE = "7";

    public static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static final DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static final String LANG_EN = "English";
    public static final String LANG_SP = "Spanish";

    //#########################################################################//
    // * Constants for language names

    // Base controller
    public static final String RIGHTS = "rights";
    public static final String SUBTITLE = "subtitle";
    public static final String LHOME = "home";
    public static final String ARCHIVE = "archive";
    public static final String DEVICES = "devices";
    public static final String LMANAGE = "manage";
    public static final String CONFIGURATION = "configuration";
    public static final String LABOUT = "about";

    // Archived
    public static final String PROJECT_COLUMN = "projectColumn";
    public static final String DATE_COLUMN = "dateColumn";
    public static final String SUBJECT_COLUMN = "subjectColumn";
    public static final String EXPORTCONFIG = "exportConfig";

    // New project
    public static final String NEWPROJECT = "newProject";
    public static final String NAMEPROMPT = "namePrompt";
    public static final String OBSERVATIONSPROMPT = "observationsPrompt";
    public static final String NAMELABEL = "nameLabel";
    public static final String OBSERVATIONSLABEL = "observationsLabel";
    public static final String STARTDATELABEL = "startDateLabel";

    // New author
    public static final String NEWAUTHOR = "newAuthor";

    // New Game
    public static final String NEWGAME = "newGame";
    public static final String NUMBUTTONSPROMPT = "numButtonsPrompt";
    public static final String NUMBUTTONSLABEL = "numButtonsLabel";

    // New Formula
    public static final String NEWFORMULA = "newFormula";

    // New Subject
    public static final String AGELABEL = "ageLabel";
    public static final String AGEPROMPT = "agePrompt";
    public static final String WEIGHTLABEL = "weightLabel";
    public static final String WEIGHTPROMPT = "weightPrompt";
    public static final String SIZELABEL = "sizeLabel";
    public static final String SIZEPROMPT = "sizePrompt";
    public static final String ISPERSON = "isPerson";
    public static final String ISOBJECT = "isObject";
    public static final String ISMALE = "isMale";
    public static final String ISFEMALE = "isFemale";

    // New Mission
    public static final String NEWMISSIONTITLE = "newMissionTitle";
    public static final String PROJECTLABEL = "projectLabel";
    public static final String AUTHORLABEL = "authorLabel";
    public static final String GAMELABEL = "gameLabel";
    public static final String SUBJECTLABEL = "subjectLabel";
    public static final String NEWPROJECTBUTTON = "newProjectButton";
    public static final String NEWGAMEBUTTON = "newGameButton";
    public static final String NEWSUBJECTBUTTON = "newSubjectButton";
    public static final String AUTHORPROMPT = "authorPrompt";
    public static final String NOSELECTION = "noSelection";
    public static final String NEWSUBJECT = "newSubject";

    // New Position
    public static final String NEWPOSITION = "newPosition";
    public static final String IMAGELABEL = "imageLabel";
    public static final String SELECTIMAGEBUTTON = "selectImageButton";

    // Configuration
    public static final String GENERAL = "general";
    public static final String IMPORTEXPORT = "importExport";
    public static final String DEFAULTS = "defaults";
    public static final String CONFIGURATIONTITLE = "configurationTitle";
    public static final String GENERALPANE = "generalPane";
    public static final String GRAPHICSPANE = "graphicsPane";
    public static final String FORMULASPANE = "formulasPane";
    public static final String GAMESPANE = "gamesPane";
    public static final String SUBJECTSPANE = "subjectsPane";
    public static final String POSITIONSPANE = "positionsPane";
    public static final String AUTHORSPANE = "authorsPane";
    public static final String IBUTTONSPANE = "iButtonsPane";

    // New Button
    public static final String NEWBUTTONTITLE = "newDevice";

    // Common
    public static final String SAVE = "save";
    public static final String UPDATE = "update";
    public static final String CANCEL = "cancel";
    public static final String CONTINUE = "continue";

}
