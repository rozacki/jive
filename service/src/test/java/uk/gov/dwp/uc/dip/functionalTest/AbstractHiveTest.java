package uk.gov.dwp.uc.dip.functionalTest;

import com.google.common.io.Resources;
import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.StandaloneHiveRunner;
import com.klarna.hiverunner.annotations.HiveSQL;
import com.opencsv.CSVReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import uk.gov.dwp.uc.dip.schemagenerator.SchemaGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingColumnsEnum.*;

@RunWith(StandaloneHiveRunner.class)
public abstract class AbstractHiveTest {
    @Rule
    public TestName name = new TestName();

    private String sourceCollection;
    String targetTableName;
    private String SourceDB;
    SchemaGenerator schemaGenerator;
    Utility util = new Utility();

    @HiveSQL(files = {}, autoStart = false)
    protected HiveShell shell;

    /**
     * The mapping file for the test. e.g. conversions_test.csv
     * @return mapping file name
     */
    abstract String getTestMappingFileName();

    /**
     * The datafile to load.  e.g. conversion_data.json
     * @return data file name
     */
    abstract String getJsonDataFileName();

    /**
     *
     * @return True to output to console contents of both tables.
     */
    abstract boolean outputSourceAndTargetTableData();

    /**
     * The results from the schema generator.  Mostly will be a call to
     * schemaGenerator.transform(hiveTargetTable).
     * There are other methods that need testing though.
     * @param hiveTargetTable The name of the table to run generation for.
     * @return List of strings containing sql for runner to run.
     */
    abstract List<String> getSchemaGeneratorResults(String hiveTargetTable);

    @Before
    public void setupEnv() throws IOException, URISyntaxException {

        getSourceInfoFromMappingFile();
        schemaGenerator = new SchemaGenerator(Resources.getResource(getTestMappingFileName()).getPath());

        String HDFSSourceJSONFileLocation = "${hiveconf:hadoop.tmp.dir}";
        schemaGenerator.setSourceJsonFileLocation(HDFSSourceJSONFileLocation);
        List<String> transformScripts = getSchemaGeneratorResults(targetTableName);

        String location = String.format("%s/%s/%s/%s",
                HDFSSourceJSONFileLocation, SourceDB,
                sourceCollection, getJsonDataFileName());
        shell.addResource(location, new File(Resources.getResource(getJsonDataFileName()).toURI()));

        shell.start();

        for (String toRun : transformScripts) {
            System.out.println(toRun);
            shell.executeQuery(toRun);
        }

        if (outputSourceAndTargetTableData()) {
            outputTableData();
        }
    }

    private void outputTableData(){
        String sourceTableName = "src_" + SourceDB + "_" + sourceCollection + "_" + targetTableName;
        sourceTableName = sourceTableName.toLowerCase();
        List<String> results = shell.executeQuery("select * from " + sourceTableName);
        for (String result : results) {
            System.out.println(result);
        }

        results = shell.executeQuery("select * from " + targetTableName);
        for (String result : results) {
            System.out.println(result);
        }
    }

    private void getSourceInfoFromMappingFile() throws URISyntaxException, IOException {
        CSVReader reader = new CSVReader(
                new FileReader(
                        new File(Resources.getResource(getTestMappingFileName()).toURI())));
        List<String[]> mapLines = reader.readAll();
        String[] mapLine1 = mapLines.get(1);
        this.SourceDB = mapLine1[SOURCE_DATABASE.getColumnNumber()];
        sourceCollection = mapLine1[SOURCE_COLLECTION.getColumnNumber()];
        targetTableName = mapLine1[TARGET_TABLE.getColumnNumber()];
    }
}
