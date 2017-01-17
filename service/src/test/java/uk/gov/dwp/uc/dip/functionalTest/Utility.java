package uk.gov.dwp.uc.dip.functionalTest;


import com.klarna.hiverunner.HiveShell;

import java.util.List;


/*
 * Created by SampathMahavithana on 06/12/2016.
 */

public class Utility {

    private final static String queryCountNotNULL = "SELECT COUNT(*) FROM %s WHERE %s IS NOT NULL";
    private final static String queryCountValue = "SELECT COUNT(*) FROM %s WHERE %s='%s'";

    public List<String> countNotNULLs(HiveShell shell, String table, String column){

        String query = String.format(queryCountNotNULL, table, column);
        return shell.executeQuery(query);
    }

    public List<String> countEqualValue(HiveShell shell, String table, String column, String value){

        String query = String.format(queryCountValue, table, column, value);
        return shell.executeQuery(query);
    }
}
