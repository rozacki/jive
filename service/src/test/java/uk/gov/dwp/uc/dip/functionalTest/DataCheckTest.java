package uk.gov.dwp.uc.dip.functionalTest;

import com.google.common.io.Resources;
import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.BeforeClass;
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

    DataCheck dataCheck = new DataCheck();
    static TechnicalMappingReader techMap;
    static List<TechnicalMapping> rules;
    List<String> result;

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

    static String getTestMappingFileNameDQ() {
        return "data_check.csv";
    }

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            techMap = TechnicalMappingReader.getInstance(Resources.getResource(getTestMappingFileNameDQ()).getPath());
            techMap.read();
            rules = techMap.getTargetColumns("targetTable");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void DQTest_firstColumn_nullability() throws Exception {
        // FIRST COLUMN:
        // generate data checks for the first column
        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(0));

        // for first column we expect two data checks
        assertTrue(dataQualityChecks.size() == 2);

        // run first check
        // order is guaranteed based based on tech mapping
        // first check is nullability
        result = shell.executeQuery(dataQualityChecks.get(0));

        // we expect to get 0
        assertArrayEquals(result.toArray(), Arrays.asList(new String("0")).toArray());

    }

    @Test
    public void DQTest_firstColumn_uniqueness() throws Exception {
        // FIRST COLUMN:
        // generate data checks for the first column
        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(0));

        // run second check - uniqueness
        result = shell.executeQuery(dataQualityChecks.get(1));

        // we expect to get 0 size
        assertArrayEquals(result.toArray(), Collections.EMPTY_LIST.toArray());

    }

    @Test
    public void DQTest_secondColumn_noChecks() throws Exception {

        // SECOND COLUMN:
        // generate data checks for the second column
        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(1));

        // for this cilumn we expect to 0 checks
        assertTrue(dataQualityChecks.size() == 0);
    }

    @Test
    public void DQTest_thirdColumn_nullability() throws Exception {

        // THIRD COLUMN:
        // generate data checks for the third column
        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(2));

        // for first column we expect two data checks
        assertTrue(dataQualityChecks.size() == 2);

        // run first check
        // order is guaranteed based based on thechnical mapping
        // first check is nullability
        result = shell.executeQuery(dataQualityChecks.get(0));

        // we expect to get 0
        assertArrayEquals(result.toArray(), Arrays.asList(new String("0")).toArray());

    }

    @Test
    public void DQTest_thirdColumn_uniqueness() throws Exception {

        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(2));

        // run second check - uniqueness
        result = shell.executeQuery(dataQualityChecks.get(1));

        // we expect to get 1 results
        assertEquals(result.size(), 1);
    }

    @Test
    public void DQTest_fourthColumn_nullability() throws Exception {

        //FOURTH COLUMNS:
        // generate data checks for the second column
        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(3));

        // for first column we expect two data checks
        assertTrue(dataQualityChecks.size() == 2);

        // run first check
        // order is guaranteed based based on thechnical mapping
        // first check is nullability
        result = shell.executeQuery(dataQualityChecks.get(0));

    }

    public void DQTest_fourthColumn_uniqueness() throws Exception {

        List<String> dataQualityChecks = dataCheck.getDataQualityChecks(rules.get(3));

        // we expect to get 1
        assertArrayEquals(result.toArray(), Arrays.asList(new String("1")).toArray());

        // run second check - uniqueness
        result = shell.executeQuery(dataQualityChecks.get(1));

        // we expect to get 0 results
        assertEquals(result.size(), 0);
    }
}
