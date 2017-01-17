package uk.gov.dwp.uc.dip.functionalTest;

import com.klarna.hiverunner.HiveShell;
import com.klarna.hiverunner.annotations.HiveSQL;
import org.junit.Assert;
import org.junit.Test;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * Created by chrisrozacki on 19/12/2016.
 */

public class ManySourcesToTarget extends AbstractHiveTest {

    @HiveSQL(files = {}, autoStart = false)
    private HiveShell shell;

    @Override
    HiveShell getHiveShell() {
        return shell;
    }

    @Override
    String getTestMappingFileName() {
        return "many_sources_to_target.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "many_sources_to_target.json";
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
        Assert.assertEquals(Collections.singletonList("3"), util.countNotNULLs(shell,HIVETargetTable,"many_sources_to_target"));

        // To test if the order in mapping works we have to check how many 1,2,3 we have in the result
        //

        Assert.assertEquals(Collections.singletonList("6"), util.countNotNULLs(shell,HIVETargetTable,"simple_to_array_to_target"));
    }
}
