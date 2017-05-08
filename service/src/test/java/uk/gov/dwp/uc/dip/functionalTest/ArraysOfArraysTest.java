package uk.gov.dwp.uc.dip.functionalTest;

/**
 * Created by chrisrozacki on 04/05/2017.
 */

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class ArraysOfArraysTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "arrays_of_arrays.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_arrays_of_arrays.json";
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
        String sql = "SELECT count(*) FROM " + targetTableName + " WHERE array1_array2 IS NOT NULL";
        List<String> results =  shell.executeQuery(sql);

        assertEquals("3", results.get(0));
    }

    @Test
    public void arraySingleElementTest(){
        String sql = "SELECT arrayElement0 FROM " + targetTableName;
        List<String> results = shell.executeQuery(sql);

        assertEquals(9, results.size());
        assertEquals("ID0", results.get(0));
    }
}

