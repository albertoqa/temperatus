package temperatus.model.pojo;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Record generated by hbm2java
 */
@Entity
@Table(name = "RECORD", schema = "PUBLIC", catalog = "DATABASE")
public class Record implements java.io.Serializable {

    private Integer id;
    private Ibutton ibutton;
    private Mission mission;
    private Position position;
    private Set<Measurement> measurements = new HashSet<Measurement>(0);

    public Record() {
    }

    public Record(Ibutton ibutton, Mission mission, Position position) {
        this.ibutton = ibutton;
        this.mission = mission;
        this.position = position;
    }

    public Record(Ibutton ibutton, Mission mission, Position position, Set<Measurement> measurements) {
        this.ibutton = ibutton;
        this.mission = mission;
        this.position = position;
        this.measurements = measurements;
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "IBUTTON_ID", nullable = false)
    public Ibutton getIbutton() {
        return this.ibutton;
    }

    public void setIbutton(Ibutton ibutton) {
        this.ibutton = ibutton;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "MISSION_ID", nullable = false)
    public Mission getMission() {
        return this.mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "POSITION_ID", nullable = false)
    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "record", cascade = CascadeType.ALL)
    public Set<Measurement> getMeasurements() {
        return this.measurements;
    }

    public void setMeasurements(Set<Measurement> measurements) {
        this.measurements = measurements;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", ibutton=" + ibutton +
                ", mission=" + mission +
                ", position=" + position +
                '}';
    }

}
