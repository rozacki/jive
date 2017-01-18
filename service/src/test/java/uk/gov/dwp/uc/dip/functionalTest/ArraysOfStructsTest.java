package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ArraysOfStructsTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "testMapping_array_of_structs.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_array_of_structs.json";
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
        String sql = "SELECT count(*) FROM " + targetTableName;
        List<String> results =  shell.executeQuery(sql);

        assertEquals("3", results.get(0));
    }

    @Test
    public void arraySingleElementTest(){
        String sql = "SELECT parentId,structure_id,structure_value,arrayElement0Id FROM "
                + targetTableName;
        List<String> results = shell.executeQuery(sql);

        // Check array explodes into 3 rows
        assertEquals(3, results.size());

        // Check id/values change, but bits outside of array stay same
        // Also that array[0] mapping stays constant.
        assertEquals("1\tID0\t100\tID0", results.get(0));
        assertEquals("1\tID2\tNULL\tID0", results.get(2));
    }
}
