package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArraysTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "testMapping_arrays.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_arrays.json";
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
    public void arrayExplodedTest(){
        String sql = "SELECT count(*) FROM " + targetTableName
                + " WHERE array3 IS NOT NULL";
        List<String> results =  shell.executeQuery(sql);

        assertEquals("9", results.get(0));
    }

    @Test
    public void arraySingleElementTest(){
        String sql = "SELECT arrayElement0 FROM " + targetTableName;
        List<String> results = shell.executeQuery(sql);

        assertEquals(9, results.size());
        assertEquals("ID0", results.get(0));
    }
}
