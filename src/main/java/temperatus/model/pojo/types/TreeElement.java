package temperatus.model.pojo.types;

import javafx.beans.property.SimpleStringProperty;
import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;

/**
 * Created by alberto on 29/1/16.
 */
public class TreeElement {

    int id;
    SimpleStringProperty name;
    SimpleStringProperty date;
    SimpleStringProperty authors;
    Class aClass;

    public TreeElement() {
    }

    public TreeElement(Project project) {
        this.id = project.getId();
        this.name = new SimpleStringProperty(project.getName());
        this.date = new SimpleStringProperty(project.getDateIni().toString());
        this.authors = new SimpleStringProperty("");
        this.aClass = Project.class;
    }

    public TreeElement(Mission mission) {
        this.id = mission.getId();
        this.name = new SimpleStringProperty(mission.getName());
        this.date = new SimpleStringProperty(mission.getDateIni().toString());
        this.authors = new SimpleStringProperty(mission.getAuthor().getName());
        this.aClass = Mission.class;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDate() {
        return date.get();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getAuthors() {
        return authors.get();
    }

    public SimpleStringProperty authorsProperty() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors.set(authors);
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }
}
