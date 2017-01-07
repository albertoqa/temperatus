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
        boolean printIndexInsteadOfTime = false;

        XSSFWorkbook wb = new XSSFWorkbook();

        try {
            File templateFile = new File(templatePath);
            XSSFWorkbook template = new XSSFWorkbook(templateFile);

            if(!isValidTemplate(template)) {
                throw new ControlledTemperatusException("");
            }

            // at this point we know that the template file is valid so lets start parsing it
            int grouping = getGroupingFromTemplate(template);
            boolean printTime = getIsPrintTimeTemplate(template);

            String timeFormat = ""; // TODO set default value
            if(printTime) {
                timeFormat = getTimeFormatFromTemplate(template);
            }

            // for each sheet do the following

            // calculate the positions/formulas to print

            // for each of the pos/form calculate the values for the given grouping
            // TODO try to do it with the lowest memory usage possible

            // create a new named sheet on the exported file
            // wb.createSheet(currentSheet.getSheetName());

            // print the time/index in all the needed places

            // print the values on the template

            // end of the export

        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wb;
    }

    private String getTimeFormatFromTemplate(XSSFWorkbook template) {
        return null;
    }

    private boolean getIsPrintTimeTemplate(XSSFWorkbook template) {
        return false;
    }

    private int getGroupingFromTemplate(XSSFWorkbook template) {
        return 0;
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

                    switch(cell.getCellType()) {
                        case Cell.CELL_TYPE_BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t\t");
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t\t");
                            break;
                        case Cell.CELL_TYPE_STRING:
                            System.out.print(cell.getStringCellValue() + "\t\t");
                            break;
                    }


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
