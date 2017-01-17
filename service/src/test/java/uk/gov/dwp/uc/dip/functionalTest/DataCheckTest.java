package uk.gov.dwp.uc.dip.functionalTest;

import com.google.common.io.Resources;
import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Test;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingReader;
import uk.gov.dwp.uc.dip.schemagenerator.datachecks.DataCheck;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Data check Functional test.
 * - transforms data
 * - generates data checks
 */
public class DataCheckTest extends AbstractHiveTest {
    @HiveSQL(files = {}, autoStart = false)
    private HiveShell shell;

    @Override
    HiveShell getHiveShell() {
        return shell;
    }

    @Override
    String getTestMappingFileName() {
        return "data_check.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "data_check.json";
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
    public void dataCheckTest() throws Exception{
        DataCheck dataCheck = new DataCheck();

        TechnicalMappingReader techMap
                = TechnicalMappingReader.getInstance(Resources.getResource(getTestMappingFileName()).getPath());
        techMap.read();

        // start with getting all rule for this target table
        List<TechnicalMapping> rules = techMap.getTargetColumns("targetTable");

        // FIRST COLUMN:
        // generate data checks for the first column
        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(0));

        // for first column we expect two data checks
        assertTrue(dataQualityChecks.size() == 2);

        // run first check
        // order is guaranteed based based on tech mapping
        // first check is nullability
        List<String> result = shell.executeQuery(dataQualityChecks.get(0));

        // we expect to get 0
        assertArrayEquals(result.toArray(), Arrays.asList(new String("0")).toArray());

        // run second check - uniqueness
        result = shell.executeQuery(dataQualityChecks.get(1));

        // we expect to get 0 size
        assertArrayEquals(result.toArray(), Collections.EMPTY_LIST.toArray());

        // SECOND COLUMN:
        // generate data checks for the second column
        dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(1));

        // for this cilumn we expect to 0 checks
        assertTrue(dataQualityChecks.size() == 0);

        // THIRD COLUMN:
        // generate data checks for the third column
        dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(2));

        // for first column we expect two data checks
        assertTrue(dataQualityChecks.size() == 2);

        // run first check
        // order is guaranteed based based on thechnical mapping
        // first check is nullability
        result = shell.executeQuery(dataQualityChecks.get(0));

        // we expect to get 0
        assertArrayEquals(result.toArray(), Arrays.asList(new String("0")).toArray());

        // run second check - uniqueness
        result = shell.executeQuery(dataQualityChecks.get(1));

        // we expect to get 1 results
        assertEquals(result.size(), 1);

        //FOURTH COLUMNS:
        // generate data checks for the second column
        dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(3));

        // for first column we expect two data checks
        assertTrue(dataQualityChecks.size() == 2);

        // run first check
        // order is guaranteed based based on thechnical mapping
        // first check is nullability
        result = shell.executeQuery(dataQualityChecks.get(0));

        // we expect to get 1
        assertArrayEquals(result.toArray(), Arrays.asList(new String("1")).toArray());

        // run second check - uniqueness
        result = shell.executeQuery(dataQualityChecks.get(1));

        // we expect to get 0 results
        assertEquals(result.size(), 0);
    }
}
