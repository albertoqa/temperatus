package temperatus.util;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.lang.Lang;
import temperatus.lang.Language;

import java.util.Date;

/**
 * Show information about on hover in graphics
 * <p>
 * Created by alberto on 24/4/16.
 */
public class ChartToolTip {

    private static final String HOVER_STYLE = "onHover";    // css style to apply

    private static Logger logger = LoggerFactory.getLogger(ChartToolTip.class.getName());

    /**
     * Browsing through the Data and applying ToolTip
     * as well as the class on hover
     */
    public static void addToolTipOnHover(XYChart.Series<Date, Number> serie, LineChart lineChart) {
        if (lineChart.getCreateSymbols()) {
            for (XYChart.Data<Date, Number> d : serie.getData()) {
                Tooltip.install(d.getNode(), new Tooltip(d.getXValue().toString() + Constants.NEW_LINE + Language.getInstance().get(Lang.TEMPERATURE_HOVER) + Constants.SPACE + Constants.decimalFormat.format(d.getYValue())));

                try {
                    d.getNode().setOnMouseEntered(event -> d.getNode().getStyleClass().add(HOVER_STYLE)); //Adding class on hover
                    d.getNode().setOnMouseExited(event -> d.getNode().getStyleClass().remove(HOVER_STYLE)); //Removing class on exit
                } catch (Exception e) {
                    logger.error("Error applying style to hover...");
                }
            }
        }
    }
}
