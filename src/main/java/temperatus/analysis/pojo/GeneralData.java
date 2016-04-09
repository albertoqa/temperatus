package temperatus.analysis.pojo;

import java.util.Date;

/**
 * Created by alberto on 2/4/16.
 */
public class GeneralData {

    String models;
    String rate;
    Date startDate;
    Date endDate;
    double maxTemp;
    double minTemp;
    double avgTemp;
    int measurementsPerButton;

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
