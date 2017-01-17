package uk.gov.dwp.uc.dip.jive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class HiveExecutor {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public String runStatements(List<String> statements) throws SQLException {

        try {
            // Register driver and create driver instance
            Class.forName(driverName);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        // TODO logging
        System.out.println("before trying to connect");
        try(Connection con =
                    DriverManager.getConnection("jdbc:hive2://"
                            + Properties.getInstance().getHiveHost()
                            + ":" + Properties.getInstance().getHivePort()
                            + "/" + Properties.getInstance().getHiveDatabase()
                            + ", " + Properties.getInstance().getHiveUser()
                            + ", " + Properties.getInstance().getHivePassword())) {


            System.out.println("connected");

            Statement stmt = con.createStatement();
            for(String statement : statements){
                stmt.execute(statement);
            }

        }
        // TODO some logging to be returned for ui.
        return "";
    }
}

