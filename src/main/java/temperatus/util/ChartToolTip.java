package temperatus.util;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import temperatus.lang.Lang;
import temperatus.lang.Language;

import java.util.Date;

/**
 * Show information about on hover in graphics
 * <p>
 * Created by alberto on 24/4/16.
 */
public class ChartToolTip {
    /**
     * Browsing through the Data and applying ToolTip
     * as well as the class on hover
     */
    public static void addToolTipOnHover(XYChart.Series<Date, Number> serie, LineChart lineChart) {
        if (lineChart.getCreateSymbols()) {
            for (XYChart.Data<Date, Number> d : serie.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(d.getXValue().toString() + "\n" + Language.getInstance().get(Lang.TEMPERATURE_HOVER) + " " + d.getYValue()));

                d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add("onHover")); //Adding class on hover
                d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove("onHover")); //Removing class on exit
            }
        }
    }
}
