package temperatus.model.pojo;

import temperatus.model.pojo.types.Unit;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Measurement generated by hbm2java
 */
@Entity
@Table(name = "MEASUREMENT", schema = "PUBLIC", catalog = "DATABASE")
public class Measurement implements java.io.Serializable {

    private Integer id;
    private Date date;
    private double data;
    private Unit unit;      // C, F
    private int recordId;

    public Measurement() {
    }

    public Measurement(Date date, double data, Unit unit) {
        this.date = date;
        this.data = data;
        this.unit = unit;
    }

    public Measurement(Date date, double data, Unit unit, int recordId) {
        this.recordId = recordId;
        this.date = date;
        this.data = data;
        this.unit = unit;
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

    @Column(name = "RECORD_ID", nullable = false)
    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
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

}
