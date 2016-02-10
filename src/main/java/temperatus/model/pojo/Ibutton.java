package temperatus.model.pojo;
// Generated 09-feb-2016 22:15:19 by Hibernate Tools 4.3.1.Final

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Ibutton generated by hbm2java
 */
@Entity
@Table(name = "IBUTTON", schema = "PUBLIC", catalog = "DATABASE", uniqueConstraints = {
        @UniqueConstraint(columnNames = "SERIAL"), @UniqueConstraint(columnNames = "ALIAS")})
public class Ibutton implements java.io.Serializable {

    private Integer id;
    private Position defaultPosition;
    private String serial;
    private String model;
    private String alias;

    public Ibutton() {
    }

    public Ibutton(String serial, String model) {
        this.serial = serial;
        this.model = model;
    }

    public Ibutton(Position defaultPosition, String serial, String model, String alias) {
        this.defaultPosition = defaultPosition;
        this.serial = serial;
        this.model = model;
        this.alias = alias;
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
    @JoinColumn(name = "DEFAULTPOS")
    public Position getPosition() {
        return this.defaultPosition;
    }

    public void setPosition(Position defaultPosition) {
        this.defaultPosition = defaultPosition;
    }

    @Column(name = "SERIAL", unique = true, nullable = false, length = 200)
    public String getSerial() {
        return this.serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Column(name = "MODEL", nullable = false, length = 200)
    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Column(name = "ALIAS", unique = true, length = 30)
    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "Ibutton{" +
                "serial='" + serial + '\'' +
                ", model='" + model + '\'' +
                ", alias='" + alias + '\'' +
                ", id=" + id +
                '}';
    }

}
