package temperatus.model.pojo;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import temperatus.model.pojo.types.Unit;
import temperatus.util.Constants;

import java.io.File;
import java.util.Date;

public class Measurement {

    private Date date;
    private SimpleDoubleProperty data = new SimpleDoubleProperty();
    private Unit unit;      // C, F
    private File file;      // location of the file containing this measurement
    private SimpleStringProperty position = new SimpleStringProperty();

    public Measurement() {
    }

    public Measurement(Date date, double data, Unit unit) {
        this.date = date;
        this.data.setValue(data);
        this.unit = unit;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getData() {
        return this.data.getValue();
    }

    public void setData(double data) {
        this.data.setValue(data);
    }

    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getPosition() {
        return position.get();
    }

    public SimpleStringProperty positionProperty() {
        return position;
    }

    public void setPosition(String position) {
        this.position.set(position);
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "date=" + date +
                ", data=" + data +
                ", unit=" + unit +
                '}';
    }

    public SimpleStringProperty getDateProperty() {
        return new SimpleStringProperty(Constants.dateTimeFormat.format(date));
    }

    public StringBinding getDataProperty() {
        return data.asString();
    }

}
