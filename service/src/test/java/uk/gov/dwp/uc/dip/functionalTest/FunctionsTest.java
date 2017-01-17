package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FunctionsTest extends AbstractHiveTest {

    @HiveSQL(files = {}, autoStart = false)
    private HiveShell shell;

    @Override
    HiveShell getHiveShell() {
        return shell;
    }

    @Override
    String getTestMappingFileName() {
        return "testMapping_functions.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_functions.json";
    }

    @Override
    boolean outputSourceAndTargetTableData() {
        return true;
    }

    @Override
    List<String> getSchemaGeneratorResults(String hiveTargetTable) {
        return schemaGenerator.transform(targetTableName);
    }

    @Test
    public void simpleFunctionTest(){
        String sql = "SELECT `my_result`, `id` FROM " + targetTableName;
        List<String> results =  shell.executeQuery(sql);

        assertEquals("11.0\t1", results.get(0));
    }

}
