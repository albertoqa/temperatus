package temperatus.exporter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import temperatus.exception.ControlledTemperatusException;
import temperatus.model.pojo.Formula;
import temperatus.model.pojo.Measurement;
import temperatus.model.pojo.Record;
import temperatus.model.pojo.types.Unit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class accepts a template file, scans it and export the data using that template.
 * <p>
 * The rules for create valid templates are: [this is just a resume, for the complete description see X]
 * - The first cell must contain a letter G followed by the wanted grouping option. For example if G2 is found, the
 * data will be printed in groups of two values (mean of the two).
 * - The user can decide where to print the positions/formulas and the time/index, the only restriction is that each of
 * them must be in a different axis -> if positions are printed on the x axis (columns) the time must be printed on the
 * y axis (rows).
 * - To specify where the positions must be printed there are several options:
 * - B1 -> indicates that we wan to print the values of one position in this row/col.
 * - B1:"Head" -> indicates that we want to print the values of the position "Head" on this row/col.
 * - B1 B2 B3 B4 -> indicates that we want to print just the values of the first 4 positions of the mission.
 * - B1 Bn -> indicates that all positions must be printed starting in the B1 cell and following the direction
 * (row-col) of the Bn cell.
 * - To specify where the time/index must be printed there are several options:
 * - T1 Tn -> uses the default time formatting.
 * - T1:"dd/mm/yyy" Tn -> uses the indicated format for the time.
 * - T1:"index" Tn -> prints indexes instead of times.
 * <p>
 * An example can be found on: resources/templates/sample.xlsx
 * <p>
 * Created by alberto on 01/01/2017.
 */
public class ExcelExporter {

    private static Logger logger = LoggerFactory.getLogger(ExcelExporter.class.getName());

    private String missionName;
    private List<Record> records;       // Records (positions) selected by the user to export
    private HashMap<Record, List<Measurement>> dataMap;     // All the records-measurements of the mission - used to calculate values of formulas
    private List<Formula> formulas;     // Formulas selected to export
    private Unit unit;                  // Unit to use in the export

    public XSSFWorkbook export(String templatePath) throws ControlledTemperatusException {

        int groupBy = 0;
        String timeFormat = "dd/mm/yy";
        boolean printIndexInsteadOfTime = false;

        XSSFWorkbook wb = new XSSFWorkbook();

        try {
            File templateFile = new File(templatePath);
            XSSFWorkbook template = new XSSFWorkbook(templateFile);

            if(!isValidTemplate(template)) {
                throw new ControlledTemperatusException("");
            }



        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wb;
    }

    private boolean isValidTemplate(XSSFWorkbook template) {

        int numberOfSheets = template.getNumberOfSheets();
        boolean checkNames = numberOfSheets > 1;

        int grouping = -1;
        boolean checkGrouping = true;

        // there must be at least one sheet
        if(numberOfSheets < 1) {
            return false;
        }

        // iterate over all sheets of the template
        for (int i = 0; i < numberOfSheets; i++) {
            logger.debug("reading sheet " + i + " from template...");
            XSSFSheet currentSheet = template.getSheetAt(i);

            // if more than one sheet check if name is valid
            if(checkNames) {
                if(!isValidName(currentSheet.getSheetName())) {
                    return false;
                }
            }

            // create a new named sheet on the exported file
            // wb.createSheet(currentSheet.getSheetName());

            // iterate through each row from current sheet
            for (Row row : currentSheet) {
                // for each row, iterate through each column
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    // if(checkGrouping) {
                    // if(.contains("G"); {
                    // grouping = .split()
                    // checkGrouping = false }




                }
            }
        }

        if(grouping == -1) {
            return false;
        }

        return false;
    }

    private boolean isValidName(String sheetName) {
        return false;
    }

}
