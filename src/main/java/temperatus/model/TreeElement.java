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

    public TreeElement() {
        this.id = -1;
        this.name = "";
        this.date = new Date();
    }

    public TreeElement(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.date = project.getDateIni();
    }

    public TreeElement(Mission mission) {

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
}
