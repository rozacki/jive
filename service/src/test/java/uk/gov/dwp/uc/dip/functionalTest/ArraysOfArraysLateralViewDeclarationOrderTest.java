package uk.gov.dwp.uc.dip.functionalTest;

/**
 * Created by chrisrozacki on 04/05/2017.
 */

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArraysOfArraysLateralViewDeclarationOrderTest extends AbstractHiveTest {

    @Override
    String getTestMappingFileName() {
        return "arrays_of_arrays_lateral_view_declaration_order.csv";
    }

    @Override
    String getJsonDataFileName() {
        return "arrays_of_arrays_lateral_view_declaration_order.json";
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
    public void orderTest() {

        // by just going here we are happy
        assertTrue(true);
    }
}

