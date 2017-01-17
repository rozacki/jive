package uk.gov.dwp.uc.dip.functionalTest;

import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by chrisrozacki on 03/01/2017.
 */
public class ManySourceSamePathToTargetTest extends AbstractHiveTest{
    @HiveSQL(files = {}, autoStart = false)
    private HiveShell shell;

    @Override
    HiveShell getHiveShell() {
        return shell;
    }

    @Override
    String getTestMappingFileName() {
        return "many_sources_same_path_to_target.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "many_sources_same_path_to_target.json";
    }

    @Override
    boolean outputSourceAndTargetTableData() {
        return true;
    }

    @Override
    List<String> getSchemaGeneratorResults(String hiveTargetTable) {
        return schemaGenerator.transform(hiveTargetTable);
    }

    /**
     * Constants used in tests
     */
    final String HIVETargetTable = "targetTable";


    @Test
    //test simple_vs_struct field
    public void selectSimpleVsStruct() throws URISyntaxException {
        //
        Assert.assertEquals(Collections.singletonList("10"), util.countNotNULLs(shell,HIVETargetTable,"target_column1"));

        Assert.assertEquals(Collections.singletonList("8"), util.countNotNULLs(shell,HIVETargetTable,"target_column2"));

        List<String> result = shell.executeQuery(String.format("select target_column1, target_column2 from %s", HIVETargetTable));

        List<String> expectedResultSet = Arrays.asList(
                new String("0\t20"),
                new String("1\t21"),
                new String("2\t22"),
                new String("3\t80"),
                new String("4\t81"),
                new String("5\t90"),
                new String("6\t23"),
                new String("40\tNULL"),
                new String("41\tNULL"),
                new String("60\tNULL"),
                new String("NULL\t71")
        );

        Assert.assertArrayEquals(expectedResultSet.toArray(), result.toArray());
    }
}
