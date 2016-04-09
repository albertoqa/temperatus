package temperatus.model.pojo;

import javafx.beans.property.SimpleStringProperty;
import temperatus.model.pojo.types.Unit;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Measurement generated by hbm2java
 */
@Entity
@Table(name = "MEASUREMENT", schema = "PUBLIC", catalog = "DATABASE")
public class Measurement implements java.io.Serializable {

    private BigInteger id;
    private Record record;
    private Date date;
    private double data;
    private Unit unit;      // C, F

    public Measurement() {
    }

    public Measurement(Date date, double data, Unit unit) {
        this.date = date;
        this.data = data;
        this.unit = unit;
    }

    public Measurement(Record record, Date date, double data, Unit unit) {
        this.record = record;
        this.date = date;
        this.data = data;
        this.unit = unit;

    }

    @Id
    @GeneratedValue(strategy = IDENTITY)

    @Column(name = "ID", unique = true, nullable = false)
    public BigInteger getId() {
        return this.id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECORD_ID", nullable = false)
    public Record getRecord() {
        return this.record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE", nullable = false, length = 23)
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Column(name = "DATA", nullable = false, precision = 17, scale = 0)
    public double getData() {
        return this.data;
    }

    public void setData(double data) {
        this.data = data;
    }

    @Column(name = "UNIT", nullable = false)
    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "date=" + date +
                ", data=" + data +
                ", unit=" + unit +
                '}';
    }


    @Transient
    public SimpleStringProperty getDateProperty() {
        return new SimpleStringProperty(date.toString());
    }

    @Transient
    public SimpleStringProperty getUnitProperty() {
        return new SimpleStringProperty(unit.toString());
    }
    @Transient
    public SimpleStringProperty getDataProperty() {
        return new SimpleStringProperty(String.valueOf(data));
    }

}
