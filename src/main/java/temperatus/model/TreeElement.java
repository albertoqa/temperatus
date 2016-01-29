package temperatus.model;

import temperatus.model.pojo.Mission;
import temperatus.model.pojo.Project;

import java.util.Date;

/**
 * Created by alberto on 29/1/16.
 */
public class TreeElement {

    int id;
    String name;
    Date date;
    String authors;
    Class aClass;

    public TreeElement() {
    }

    public TreeElement(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.date = project.getDateIni();
        this.authors = "";
        this.aClass = Project.class;
    }

    public TreeElement(Mission mission) {
        this.id = mission.getId();
        this.name = mission.getName();
        this.date = mission.getDateIni();
        this.authors = mission.getAuthor();
        this.aClass = Mission.class;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }
}
