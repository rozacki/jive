package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/***
 * This is to test the result for generated SQL of deleted documents containing arrays.
 * Example of generated SQL, note it does not use COALESCE() function
 *  LATERAL VIEW OUTER EXPLODE(`_removed`.`field2`) view_exploded_fieldRemoved AS exploded_fieldRemoved
    LATERAL VIEW OUTER EXPLODE(`field2`) view_exploded_field AS exploded_field
 */
public class LogicalDeleteArrayTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "logical_delete_array.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "logical_delete_array.json";
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
    public void explodeNotNullTest(){

        List<String>  results = util.countNotNULLs(shell, "targettable", "logical_delete_field1");

        assertEquals("12", results.get(0));
    }

    @Test
    public void deletedDocumentColumnValuesTest(){

        List<String>  results = util.countEqualValue(shell, "targettable", "logical_delete_field1", "field1_value");

        assertEquals("3", results.get(0));

        results = util.countEqualValue(shell, "targettable", "logical_delete_field2", "removed_field2_item1");

        assertEquals("3", results.get(0));
    }
}
