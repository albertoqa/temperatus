package temperatus.model.pojo.types;

import javafx.beans.property.SimpleStringProperty;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;
import temperatus.util.Constants;

/**
 * Element that holds the data for the archive view tree table
 * <p>
 * Created by alberto on 29/1/16.
 */
public class TreeElement {

    TreeElementType type;
    Project project;
    Mission mission;

    public TreeElement() {
    }

    public TreeElement(Project project) {
        this.project = project;
        this.type = TreeElementType.Project;
    }

    public TreeElement(Mission mission) {
        this.mission = mission;
        this.type = TreeElementType.Mission;
    }


    public SimpleStringProperty getName() {
        if (type == TreeElementType.Project) {
            return new SimpleStringProperty(project.getName());
        } else {
            return new SimpleStringProperty(mission.getName());
        }
    }

    public SimpleStringProperty getDate() {
        if (type == TreeElementType.Project) {
            return new SimpleStringProperty(Constants.dateFormat.format(project.getDateIni()));
        } else {
            return new SimpleStringProperty(Constants.dateFormat.format(mission.getDateIni()));
        }
    }

    public SimpleStringProperty getSubject() {
        if (type == TreeElementType.Project) {
            return new SimpleStringProperty("");
        } else {
            return new SimpleStringProperty(mission.getSubject().getName());
        }
    }

    public <T> T getElement() {
        if (type == TreeElementType.Project) {
            return (T) project;
        } else {
            return (T) mission;
        }
    }

    public TreeElementType getType() {
        return type;
    }

}

