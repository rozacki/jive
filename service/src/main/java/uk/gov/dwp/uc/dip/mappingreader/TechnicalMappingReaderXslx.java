package uk.gov.dwp.uc.dip.mappingreader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

import static uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingColumnsEnum.*;

/**
 * Reads an Excel spreadsheet
 * Created by paul on 08/12/16.
 */
class TechnicalMappingReaderXslx extends TechnicalMappingReader {

    private final static String SheetMapping    =   "Transform";

    TechnicalMappingReaderXslx(String filePath) {
        super(filePath);
    }

    @Override
    public void read() throws IOException {
        if (filePath != null) {
            // Finds the workbook instance for XLSX file
            XSSFWorkbook workBook = new XSSFWorkbook(new FileInputStream(filePath));

            // Return second sheet from the XLSX workbook
            XSSFSheet sheet = workBook.getSheet(SheetMapping);

            int i = 0;
            int lineNo = 0;
            for (Row row : sheet) {
                // Two first rows are headers
                if (i++ < 1) {
                    continue;
                }
                TechnicalMapping tm = new TechnicalMapping();
                tm.sourceFileLineNo = ++ lineNo;
                tm.sourceDatabase = row.getCell(SOURCE_DATABASE.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                tm.sourceCollection = row.getCell(SOURCE_COLLECTION.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                tm.targetTableName = row.getCell(TARGET_TABLE.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                tm.targetFieldName = row.getCell(TARGET_FIELD.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                tm.jsonPath = row.getCell(SOURCE_PATH.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                tm.sourceType = MappingTypeEnum.getByTypeName(
                        row.getCell(SOURCE_DATA_TYPE.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue());
                tm.targetType = MappingTypeEnum.getByTypeName(
                        row.getCell(TARGET_DATA_TYPE.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue());
                tm.function = row.getCell(FUNCTION.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                tm.setUserDefinedSourceType(row.getCell(SOURCE_DATA_TYPE.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue());
                tm.setUserDefinedTargetType(row.getCell(TARGET_DATA_TYPE.columnNumber,Row.CREATE_NULL_AS_BLANK).getStringCellValue());

                rules.add(tm);
            }
        }
    }

    @Override
    int getColumnCount() {
        XSSFWorkbook workBook;
        try {
            workBook = new XSSFWorkbook(new FileInputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        XSSFSheet sheet = workBook.getSheet(SheetMapping);
        Row row = sheet.getRow(0);
        return row.getLastCellNum();
    }
}
