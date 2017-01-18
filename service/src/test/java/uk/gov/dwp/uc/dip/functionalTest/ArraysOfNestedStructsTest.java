package uk.gov.dwp.uc.dip.functionalTest;

import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by sampathmahavithana on 04/01/2017.
 */
public class ArraysOfNestedStructsTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "testMapping_array_of_nested_structs.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_array_of_nested_structs.json";
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
        String sql = "SELECT * FROM " + targetTableName;
        List<String> results = shell.executeQuery(sql);

        // Check array explodes into 4 rows
        assertEquals(4, results.size());
    }

    @Test
    public void arraySingleElementTest(){
        String sql = "SELECT parentId,structure_id1,structure_value1,structure_id2,structure_value2 FROM "
                + targetTableName;
        List<String> results = shell.executeQuery(sql);

        // Check array explodes into 4 rows
        assertEquals(4, results.size());

        // Check id/values change, but bits outside of array stay same
        // Also that array[0] mapping stays constant.
        assertEquals("1\tID3\t100\tNULL\tNULL", results.get(0));
        assertEquals("1\tNULL\tNULL\tID5\t200", results.get(1));
        assertEquals("2\tID3\t1000\tNULL\tNULL", results.get(2));
        assertEquals("2\tNULL\tNULL\tID5\t2000", results.get(3));
    }
}
