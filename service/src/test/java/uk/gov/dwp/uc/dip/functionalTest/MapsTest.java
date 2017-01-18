package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MapsTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "testMapping_maps.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_maps.json";
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
    public void simpleMapTest(){
        String sql = "SELECT `mapKey`,`mapValue`, `id` FROM " + targetTableName;
        List<String> results =  shell.executeQuery(sql);

        assertEquals("map_key_data\t42\t1", results.get(0));
    }

}
