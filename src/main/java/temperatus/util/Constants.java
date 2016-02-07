package temperatus.util;

import java.util.prefs.Preferences;

/**
 * Created by alberto on 17/1/16.
 */
public class Constants {

    //#########################################################################//
    //* Convenience constants for fxml layouts

    public static final String SPLASH = "/fxml/Welcome.fxml";

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
    public static final String NEW_POSITION = "/fxml/NewPosition.fxml";
    public static final String RECORD_CONFIG = "/fxml/RecordConfig.fxml";
    public static final String RECORD_INFO = "/fxml/RecordInfo.fxml";

    public static final String CONNECTED = "/fxml/ConnectedDevices.fxml";

    //#########################################################################//

    public static Preferences prefs = Preferences.userRoot().node("temperatus");

    public static final String LANGUAGE = "language";
    public static final String DEFAULT_LANGUAGE = "en_US";
    public static final String PREFRANGE = "prefRange";
    public static final String DEFAULT_RANGE = "7";

    public static final double MIN_HEIGHT = 800.0;
    public static final double MIN_WIDTH = 1000.0;

    //#########################################################################//
    // * Constants for language names

    // Base controller
    public static final String RIGHTS = "rights";
    public static final String LHOME = "home";
    public static final String ARCHIVE = "archive";
    public static final String DEVICES = "devices";
    public static final String CONFIGURATION = "configuration";

    // New project
    public static final String NEWPROJECT = "newProject";
    public static final String NAMEPROMPT = "namePrompt";
    public static final String OBSERVATIONSPROMPT = "observationsPrompt";
    public static final String NAMELABEL = "nameLabel";
    public static final String OBSERVATIONSLABEL = "observationsLabel";
    public static final String STARTDATELABEL = "startDateLabel";

    // New Game
    public static final String NEWGAME = "newGame";
    public static final String NUMBUTTONSPROMPT = "numButtonsPrompt";
    public static final String NUMBUTTONSLABEL = "numButtonsLabel";

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


    // Common
    public static final String SAVE = "save";
    public static final String CANCEL = "cancel";
    public static final String CONTINUE = "continue";

}
