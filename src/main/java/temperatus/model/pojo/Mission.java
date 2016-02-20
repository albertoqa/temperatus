package temperatus.model.pojo;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Mission generated by hbm2java
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "MISSION", schema = "PUBLIC", catalog = "DATABASE", uniqueConstraints = @UniqueConstraint(columnNames = "NAME"))
public class Mission implements java.io.Serializable {

    private int id;
    private Author author;
    private Game game;
    private Project project;
    private Subject subject;
    private String name;
    private Date dateIni;
    private String observations;
    private Set<Record> records = new HashSet<Record>(0);

    public Mission() {
    }

    public Mission(Author author, Game game, Project project, Subject subject, String name, Date dateIni) {
        this.author = author;
        this.game = game;
        this.project = project;
        this.subject = subject;
        this.name = name;
        this.dateIni = dateIni;
    }

    public Mission(Author author, Game game, Project project, Subject subject, String name, Date dateIni,
                   String observations) {
        this.author = author;
        this.game = game;
        this.project = project;
        this.subject = subject;
        this.name = name;
        this.dateIni = dateIni;
        this.observations = observations;
    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "ID", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "AUTHOR_ID", nullable = false)
    public Author getAuthor() {
        return this.author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "GAME_ID", nullable = false)
    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "SUBJECT_ID", nullable = false)
    public Subject getSubject() {
        return this.subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Column(name = "NAME", unique = true, nullable = false, length = 100)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mission", cascade = CascadeType.ALL)
    public Set<Record> getRecords() {
        return this.records;
    }

    public void setRecords(Set<Record> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "name='" + name + '\'' +
                ", dateIni=" + dateIni +
                '}';
    }

}
