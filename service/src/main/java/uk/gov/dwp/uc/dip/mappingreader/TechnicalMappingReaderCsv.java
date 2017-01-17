package uk.gov.dwp.uc.dip.mappingreader;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingColumnsEnum.*;

/**
 * Reads a CSV file and populates list of TechnicalMappingObjects
 * Created by paul on 08/12/16.
 */
class TechnicalMappingReaderCsv extends TechnicalMappingReader {

    TechnicalMappingReaderCsv(String filePath) {
        super(filePath);
    }

    @Override
    public void read() throws IOException {
        CSVReader reader = new CSVReader(new FileReader(filePath));

        List<String[]> rows=reader.readAll();

        int i = 0;
        int lineNo = 0;
        for(String[] row:rows){
            // Two first rows are headers
            if (i++ < 1) {
                continue;
            }

            TechnicalMapping tm = new TechnicalMapping();
            tm.sourceFileLineNo = ++lineNo;
            tm.sourceDatabase = row[SOURCE_DATABASE.columnNumber];
            tm.sourceCollection = row[SOURCE_COLLECTION.columnNumber];
            tm.targetTableName = row[TARGET_TABLE.columnNumber];
            tm.targetFieldName = row[TARGET_FIELD.columnNumber];
            tm.jsonPath =  row[SOURCE_PATH.columnNumber];
            tm.sourceType = MappingTypeEnum.getByTypeName(row[SOURCE_DATA_TYPE.columnNumber]);
            tm.targetType = MappingTypeEnum.getByTypeName(row[TARGET_DATA_TYPE.columnNumber]);
            tm.function = row[FUNCTION.columnNumber];
            tm.setUserDefinedSourceType(row[SOURCE_DATA_TYPE.columnNumber]);
            tm.setUserDefinedTargetType(row[TARGET_DATA_TYPE.columnNumber]);

            try {
                tm.dataChecksString = row[TARGET_CONSTRAINTS.columnNumber];
            }catch(Exception ex) {
                // catch and swallow
            }

            rules.add(tm);
        }
    }

    @Override
    int getColumnCount()  {

        String firstLine;
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            firstLine = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        return firstLine.split(",").length;
    }


}
