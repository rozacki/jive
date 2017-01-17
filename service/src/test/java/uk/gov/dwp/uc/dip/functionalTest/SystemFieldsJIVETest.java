package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.StandaloneHiveRunner;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by SampathMahavithana on 06/12/2016.
 */
@RunWith(StandaloneHiveRunner.class)
public class  SystemFieldsJIVETest extends AbstractHiveTest {

    @HiveSQL(files = {}, autoStart = false)
    private HiveShell shell;

    @Override
    HiveShell getHiveShell() {
        return shell;
    }

    @Override
    String getTestMappingFileName() {
        return "testMapping_systemFields.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_systemFields.json";
    }

    @Override
    boolean outputSourceAndTargetTableData() {
        return true;
    }

    @Override
    List<String> getSchemaGeneratorResults(String hiveTargetTable) {
        return schemaGenerator.transform(hiveTargetTable);
    }

    @Test
    public void countNotNullVersion() {

        assertEquals(Collections.singletonList("1"),
                util.countNotNULLs(shell,targetTableName,"version"));
    }

    @Test
    public void confirmExistingCreatedTsMappingUsed() {

        assertEquals(Collections.singletonList("1"),
                util.countNotNULLs(shell,targetTableName,"created_ts"));
        List<Object[]> results = shell.executeStatement("select created_ts from " + targetTableName +
                " where created_ts is not null");
        Object[] a = results.get(0);
        assertEquals("2016-09-24 00:01:00.0", a[0]);
    }

    @Test
    public void countNotNullLastModifiedTs() {

        assertEquals(Collections.singletonList("1"),
                util.countNotNULLs(shell,targetTableName,"last_modified_ts"));
    }

    @Test
    public void countNotNullRemovedTs() {

        assertEquals(Collections.singletonList("1"),
                util.countNotNULLs(shell,targetTableName,"removed_ts"));
    }
}
