package temperatus.exporter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by alberto on 01/01/2017.
 */
public class ExcelExporter {

    private static Logger logger = LoggerFactory.getLogger(ExcelExporter.class.getName());

    public XSSFWorkbook export(String templatePath) {

        XSSFWorkbook wb = new XSSFWorkbook();

        try {
            File templateFile = new File(templatePath);
            XSSFWorkbook template = new XSSFWorkbook(templateFile);

            // iterate over all sheets of the template
            for(int i = 0; i < template.getNumberOfSheets(); i++) {
                logger.debug("reading sheet " + i + " from template...");
                XSSFSheet currentSheet = template.getSheetAt(i);

                // create a new named sheet on the exported file
                wb.createSheet(currentSheet.getSheetName());

                // iterate through each row from current sheet
                for (Row row : currentSheet) {
                    // for each row, iterate through each column
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();











                    }
                }
            }

        } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
        }

        return wb;
    }


}
