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

    private TreeElementType type;
    private Project project;
    private Mission mission;

    /**
     * Element will be a project
     * @param project project to show
     */
    public TreeElement(Project project) {
        this.project = project;
        this.type = TreeElementType.Project;
    }

    /**
     * Element will be a mission
     * @param mission mission to show
     */
    public TreeElement(Mission mission) {
        this.mission = mission;
        this.type = TreeElementType.Mission;
    }

    /**
     * Return the name of the element stored in this instance
     * @return name of the element
     */
    public SimpleStringProperty getName() {
        if (type == TreeElementType.Project) {
            return project.getNameProperty();
        } else {
            return mission.getNameProperty();
        }
    }

    /**
     * Return the date of the element stored in this instance
     * @return date of the element
     */
    public SimpleStringProperty getDate() {
        if (type == TreeElementType.Project) {
            return new SimpleStringProperty(Constants.dateFormat.format(project.getDateIni()));
        } else {
            return new SimpleStringProperty(Constants.dateFormat.format(mission.getDateIni()));
        }
    }

    /**
     * Return the subject of the element stored in this instance, only if element is a mission
     * @return subject of the element
     */
    public SimpleStringProperty getSubject() {
        if (type == TreeElementType.Project) {
            return new SimpleStringProperty("");
        } else {
            return mission.getSubject().getNameProperty();
        }
    }

    /**
     * Return element stored in this instance
     * @return element stored
     */
    public <T> T getElement() {
        if (type == TreeElementType.Project) {
            return (T) project;
        } else {
            return (T) mission;
        }
    }

    /**
     * Type of element stored in this instance
     * @return type of element
     */
    public TreeElementType getType() {
        return type;
    }

    @Override
    public String toString() {
        if (type == TreeElementType.Project) {
            return project.getName() + " " + Constants.dateFormat.format(project.getDateIni());
        } else {
            return mission.getName() + " " + mission.getSubject().getName() + " " + Constants.dateFormat.format(mission.getDateIni()) + " " + mission.getProject().getName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeElement)) return false;

        TreeElement that = (TreeElement) o;

        if (getType() != that.getType()) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        return mission != null ? mission.equals(that.mission) : that.mission == null;

    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (mission != null ? mission.hashCode() : 0);
        return result;
    }
}

