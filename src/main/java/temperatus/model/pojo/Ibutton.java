package temperatus.model.pojo;
// Generated 09-feb-2016 22:15:19 by Hibernate Tools 4.3.1.Final

import javafx.beans.property.SimpleStringProperty;

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
    private SimpleStringProperty serial = new SimpleStringProperty();
    private SimpleStringProperty model = new SimpleStringProperty();
    private SimpleStringProperty alias = new SimpleStringProperty();

    public Ibutton() {
    }

    public Ibutton(String serial, String model) {
        this.serial.setValue(serial);
        this.model.setValue(model);
    }

    public Ibutton(Position defaultPosition, String serial, String model, String alias) {
        this.defaultPosition = defaultPosition;
        this.serial.setValue(serial);
        this.model.setValue(model);
        this.alias.setValue(alias);
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
        return this.serial.getValue();
    }

    public void setSerial(String serial) {
        this.serial.setValue(serial);
    }

    @Column(name = "MODEL", nullable = false, length = 200)
    public String getModel() {
        return this.model.getValue();
    }

    public void setModel(String model) {
        this.model.setValue(model);
    }

    @Column(name = "ALIAS", unique = true, length = 30)
    public String getAlias() {
        return this.alias.getValue();
    }

    public void setAlias(String alias) {
        this.alias.setValue(alias);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ibutton)) return false;

        Ibutton ibutton = (Ibutton) o;

        if (getSerial() != null ? !getSerial().equals(ibutton.getSerial()) : ibutton.getSerial() != null) return false;
        if (getModel() != null ? !getModel().equals(ibutton.getModel()) : ibutton.getModel() != null) return false;
        return !(getAlias() != null ? !getAlias().equals(ibutton.getAlias()) : ibutton.getAlias() != null);

    }

    @Override
    public int hashCode() {
        int result = getSerial() != null ? getSerial().hashCode() : 0;
        result = 31 * result + (getModel() != null ? getModel().hashCode() : 0);
        result = 31 * result + (getAlias() != null ? getAlias().hashCode() : 0);
        return result;
    }

    @Transient
    public SimpleStringProperty getModelProperty() {
        return model;
    }

    @Transient
    public SimpleStringProperty getSerialProperty() {
        return serial;
    }

    @Transient
    public SimpleStringProperty getAliasProperty() {
        return alias;
    }

    @Transient
    public SimpleStringProperty getPositionProperty() {
        if (getPosition() != null) {
            return getPosition().getPlaceProperty();
        }
        return new SimpleStringProperty("");
    }

}
