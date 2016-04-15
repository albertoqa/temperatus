package temperatus.analysis.pojo;

import java.util.Date;

/**
 * General data of a mission, used to show information to the user in the general TAB of RecordConfigController
 * <p>
 * Created by alberto on 2/4/16.
 */
public class GeneralData {

    private String models;  // model/s of device used to measure the temperature
    private String rate;    // rate used to measure
    private Date startDate; // start date of the experiment
    private Date endDate;   // end date of the experiment
    private double maxTemp; // maximum temperature registered
    private double minTemp; // minimum temperature registered
    private double avgTemp; // average temperature registered
    private int measurementsPerButton;  // average number of measurements per device

    public String getModels() {
        return models;
    }

    public void setModels(String models) {
        this.models = models;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getAvgTemp() {
        return avgTemp;
    }

    public void setAvgTemp(double avgTemp) {
        this.avgTemp = avgTemp;
    }

    public int getMeasurementsPerButton() {
        return measurementsPerButton;
    }

    public void setMeasurementsPerButton(int measurementsPerButton) {
        this.measurementsPerButton = measurementsPerButton;
    }
}
