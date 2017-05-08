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
    public void countTest() {
        String sql = "SELECT count(*) FROM " + targetTableName + " WHERE array1_array2 IS NOT NULL";
        List<String> results = shell.executeQuery(sql);

        assertEquals("6", results.get(0));
    }
    @Test
    public void rowsTest(){
        String sql = "SELECT array1_array2 FROM " + targetTableName + " WHERE array1_array2 IS NOT NULL";
        List<String> results =  shell.executeQuery(sql);
        assertEquals("a", results.get(0));
        assertEquals("b", results.get(1));
        assertEquals("c", results.get(2));
        assertEquals("a", results.get(3));
        assertEquals("b", results.get(4));
        assertEquals("c", results.get(5));
    }

    @Test
    public void nestedArrayAndStructCount(){
        String sql = "SELECT count(*) FROM " + targetTableName + " WHERE array1_array3_a IS NOT NULL";
        List<String> results =  shell.executeQuery(sql);
        assertEquals("6", results.get(0));
    }

    @Test
    public void nestedArrayAndStructRows(){
        String sql = "SELECT array1_array3_a,array1_array3_b FROM " + targetTableName + " WHERE array1_array3_a is not null and array1_array3_b IS NOT NULL";
        List<String> results =  shell.executeQuery(sql);
        assertEquals("a1\tb1", results.get(0));
        assertEquals("a2\tb2", results.get(1));
        assertEquals("a3\tb3", results.get(2));
        assertEquals("a1\tb1", results.get(3));
        assertEquals("a2\tb2", results.get(4));
        assertEquals("a3\tb3", results.get(5));
    }

    @Test
    public void arraySingleElementWithRemvedTest(){
        String sql = "SELECT array1_array4_array4_index FROM " + targetTableName + " where  array1_array4_array4_index is not null";
        List<String> results = shell.executeQuery(sql);

        assertEquals(2, results.size());
    }

    @Test
    public void nestedArrayAndIndexRowsTest(){
        String sql = "SELECT array1_array4_array4_index FROM " + targetTableName + " where  array1_array4_array4_index is not null";
        List<String> results = shell.executeQuery(sql);

        assertEquals("1", results.get(0));
        assertEquals("2", results.get(1));
    }

}

