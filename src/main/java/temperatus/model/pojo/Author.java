package temperatus.model.pojo;

import javafx.beans.property.SimpleStringProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Author generated by hbm2java
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "AUTHOR", schema = "PUBLIC", catalog = "DATABASE", uniqueConstraints = @UniqueConstraint(columnNames = "NAME"))
public class Author implements java.io.Serializable {

    private Integer id;
    private String name;
    private Set<Mission> missions = new HashSet<Mission>(0);

    public Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public Author(String name, Set<Mission> missions) {
        this.name = name;
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

    @Column(name = "NAME", unique = true, nullable = false, length = 100)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL)
    public Set<Mission> getMissions() {
        return this.missions;
    }

    public void setMissions(Set<Mission> missions) {
        this.missions = missions;
    }

    @Override
    public String toString() {
        return name;
    }

    @Transient
    public SimpleStringProperty getNameProperty() {
        return new SimpleStringProperty(getName());
    }

}
