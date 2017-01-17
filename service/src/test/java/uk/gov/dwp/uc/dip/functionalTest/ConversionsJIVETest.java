package uk.gov.dwp.uc.dip.functionalTest;

import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

    /*
    * Created by SampathMahavithana on 06/12/2016.
    */

public class ConversionsJIVETest extends AbstractHiveTest{

    @HiveSQL(files = {}, autoStart = false)
    private HiveShell shell;

    @Test
    public void countNotNullStrings() {

        assertEquals(Collections.singletonList("1"), util.countNotNULLs(shell, targetTableName,"string_column"));
    }

    @Test
    public void countNotNullInts() {

        assertEquals(Collections.singletonList("1"), util.countNotNULLs(shell, targetTableName,"int_column"));
    }

    @Test
    public void countNotNullDoubles() {

        assertEquals(Collections.singletonList("1"), util.countNotNULLs(shell, targetTableName,"double_column"));
    }

    @Test
    public void countNotNullDates() {

        assertEquals(Collections.singletonList("1"), util.countNotNULLs(shell, targetTableName,"date_column"));
    }

    @Test
    public void countNotNullTimestamps() {

        assertEquals(Collections.singletonList("1"), util.countNotNULLs(shell, targetTableName,"time_column"));
    }

    @Test
    public void countNotNullBooleans() {

        assertEquals(Collections.singletonList("1"), util.countNotNULLs(shell, targetTableName,"bool_column"));
        List<Object[]> results = shell.executeStatement("select bool_column from " + targetTableName +
                " where bool_column is not null");
        Object[] a = results.get(0);
        assertEquals(false, a[0]);
    }

    @Override
    HiveShell getHiveShell() {
        return shell;
    }

    @Override
    String getTestMappingFileName() {
        return "testMapping_conversions.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_conversions.json";
    }

    @Override
    boolean outputSourceAndTargetTableData() {
        return true;
    }

    @Override
    List<String> getSchemaGeneratorResults(String hiveTargetTable) {
        return schemaGenerator.transform(hiveTargetTable);
    }
}
