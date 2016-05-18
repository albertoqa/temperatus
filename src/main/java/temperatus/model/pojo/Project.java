package temperatus.model.pojo;
// Generated 09-feb-2016 22:15:19 by Hibernate Tools 4.3.1.Final

import javafx.beans.property.SimpleStringProperty;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Project generated by hbm2java
 */
@Entity
@Table(name = "PROJECT", schema = "PUBLIC", catalog = "DATABASE", uniqueConstraints = @UniqueConstraint(columnNames = "NAME"))
public class Project implements java.io.Serializable {

    private Integer id;
    private SimpleStringProperty name = new SimpleStringProperty();
    private Date dateIni;
    private String observations;
    private Set<Mission> missions = new HashSet<Mission>(0);

    public Project() {
    }

    public Project(String name, Date dateIni) {
        this.name.setValue(name);
        this.dateIni = dateIni;
    }

    public Project(String name, Date dateIni, String observations) {
        this.name.setValue(name);
        this.dateIni = dateIni;
        this.observations = observations;
    }

    public Project(String name, Date dateIni, String observations, Set<Mission> missions) {
        this.name.setValue(name);
        this.dateIni = dateIni;
        this.observations = observations;
        this.missions = missions;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "ID", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "NAME", unique = true, nullable = false)
    public String getName() {
        return this.name.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_INI", nullable = false, length = 8)
    public Date getDateIni() {
        return this.dateIni;
    }

    public void setDateIni(Date dateIni) {
        this.dateIni = dateIni;
    }

    @Column(name = "OBSERVATIONS")
    public String getObservations() {
        return this.observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade = CascadeType.ALL)
    public Set<Mission> getMissions() {
        return this.missions;
    }

    public void setMissions(Set<Mission> missions) {
        this.missions = missions;
    }

    @Override
    public String toString() {
        return name.getValue();
    }

    @Transient
    public SimpleStringProperty getNameProperty() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;

        Project project = (Project) o;

        if (getName() != null ? !getName().equals(project.getName()) : project.getName() != null) return false;
        return getDateIni() != null ? getDateIni().equals(project.getDateIni()) : project.getDateIni() == null;

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getDateIni() != null ? getDateIni().hashCode() : 0);
        return result;
    }
}
