package uk.gov.dwp.uc.dip.jive.hiverun;

import com.vaadin.data.util.BeanItemContainer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.jive.NotificationUtils;
import uk.gov.dwp.uc.dip.jive.Properties;
import uk.gov.dwp.uc.dip.jive.User;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.lang.reflect.Constructor;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *   Provides a connection to HIVE as a proxy user to execute statements
 *   as lower privileges logged in user.  Gets Kerberos token for proxy.
 */
public class HiveProxyExecutor {

    private final static Logger log = Logger.getLogger(HiveProxyExecutor.class);
    //  JDBC credentials
    private static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";
    private static final String JDBC_DB_URL = "jdbc:hive2://<HOST>:<PORT>/default;" +
            "principal=<PRINCIPAL>/<HOST>@<REALM>;" +
            "hive.server2.proxy.user=<PROXY_USER>";

    public HiveProxyExecutor() {
        log.debug("Setting jaas config file location.");
        log.debug("Location=" + Properties.getInstance().getJaasConfFile());
        System.setProperty("java.security.auth.login.config", Properties.getInstance().getJaasConfFile());
    }

    private String getJdbcUrl(){
        log.debug("Building JDBC URL.");
        Properties props = Properties.getInstance();
        String uri = JDBC_DB_URL.replace("<HOST>", props.getHiveHost())
                .replace("<PORT>",props.getHivePort())
                .replace("<REALM>", props.getHiveHostRealm())
                .replace("<PROXY_USER>", User.getUserName())
                .replace("<PRINCIPAL>", props.getHivePrincipalUser());
        log.info("JDBC URI: " + uri);
        return uri;
    }

    public List<List<Object>> executeSingleStatement(String statement, String database, BeanItemContainer<StatementResult> container) {
        List<String> statements = new ArrayList<String>();
        statements.add(statement);
        return executeMultipleStatements(statements,  database, container);
    }

    public List<List<Object>> executeMultipleStatements(List<String> statements, String database, BeanItemContainer<StatementResult> container) {
        if (Properties.getInstance().isAuthenticationDisabled()) {
            return executeMultipleStatementsNoAuthImpl(statements, database, container);
        } else {
            return executeMultipleStatementsImpl(statements, database, container);
        }
    }

    List<List<Object>> executeMultipleStatementsImpl(
            List<String> statements, String database, BeanItemContainer<StatementResult> container) {

        log.debug(String.format("sql statements %d",statements.size()));
        Subject subject = null;
        int order = 1;

        if (database == null) {
            log.debug("database is missing");
        }else{
            log.debug("database is set to "+ database);
        }

        try {
            subject = login();
        } catch (LoginException e) {
            NotificationUtils.displayError(e);
            log.error("Failed login (for proxy jdbc)", e);
            container.addItem(new StatementResult(false, e.getLocalizedMessage(), order++));
        }

        if(null != subject) {
            HackToGetSubjectDoAsWorking(subject);
            log.debug("Connecting to HIVE.");
            try {
                try (
                        Connection conn = (Connection) Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
                                 public Object run() throws SQLException, ClassNotFoundException {
                                     Connection con;
                                     Class.forName(JDBC_DRIVER);
                                     con = DriverManager.getConnection(getJdbcUrl(), null, null);
                                     return con;
                                 }
                             }); Statement stmt = conn.createStatement()) {
                    if(StringUtils.trimToNull(database) != null) {
                        log.debug("Switching to database " + database);
                        stmt.execute("use " + database);
                    }
                    for (String sql : statements) {
                            log.debug("Running:" + StringUtils.left(sql, 50) + "...");
                            boolean hasResult = stmt.execute(sql);
                            container.addBean(new StatementResult(true, sql, order++));
                            if(hasResult){
                                //we return first that have
                                return convertResutlSetToTable(stmt.getResultSet());
                            }
                    }
                }
            } catch (SQLException | PrivilegedActionException e) {
                NotificationUtils.displayError(e);
                log.error(e);
                container.addItem(new StatementResult(false, e, order));
            }
        }else{
            log.error("Null Kerberos subject");
            container.addItem(new StatementResult(false, "No Hive login subject", order));
        }
        return null;
    }

    List<List<Object>> executeMultipleStatementsNoAuthImpl(
            List<String> statements, String database, BeanItemContainer<StatementResult> container) {

        log.debug(String.format("sql statements %d",statements.size()));
        int order = 1;

        if (database == null) {
            log.debug("database is missing");
        }else{
            log.debug("database is set to "+ database);
        }

        log.debug("Connecting to HIVE.");
        try {
            try (
                Connection conn = DriverManager.getConnection("jdbc:hive2://localhost:10000/default", null, null);
                Statement stmt = conn.createStatement()) {
                if(StringUtils.trimToNull(database) != null) {
                    log.debug("Switching to database " + database);
                    stmt.execute("use " + database);
                }
                for (String sql : statements) {
                    log.debug("Running:" + StringUtils.left(sql, 50) + "...");
                    boolean hasResult = stmt.execute(sql);
                    container.addBean(new StatementResult(true, sql, order++));
                    if(hasResult){
                        //we return first that have
                        return convertResutlSetToTable(stmt.getResultSet());
                    }
                }
            }
        }catch (SQLException e) {
            NotificationUtils.displayError(e);
            log.error(e);
            container.addItem(new StatementResult(false, e, order));
        }
        return null;
    }
    private List<List<Object>> convertResutlSetToTable(ResultSet rs) throws SQLException{
        List<List<Object>> table = new ArrayList<>();
        List<Object> columns = new ArrayList<>();
        for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
            columns.add(rs.getMetaData().getColumnName(i));
        }
        table.add(columns);

        while(rs.next()){
            columns = new ArrayList<>();
            for(int i=1;i<=rs.getMetaData().getColumnCount();i++){
                columns.add(rs.getObject(i));
            }
            table.add(columns);
        }

        return table;
    }

    private Subject login() throws LoginException {
        LoginContext lc;
        Subject signedOnUserSubject = null;

        log.debug("Creating LoginContext");
        try {
            lc = new LoginContext("JiveClient");
            lc.login();
            // get the Subject that represents the signed-on user
            signedOnUserSubject = lc.getSubject();
            log.debug("Logged in as" + signedOnUserSubject.toString());
        }catch (SecurityException e){
            log.error(e);
            NotificationUtils.displayError(e);
        }

       return signedOnUserSubject;
    }

    // Hack to add an Dummy User to the list of principals in the subject
    // This is not needed for functionality but to bypass the check in
    // UserGroupInformation.getCurrentUser() till Hadoop/Hive formally supports multi-user kerberos.
    // Using java reflection coz the "User" class is non-public
    private static void HackToGetSubjectDoAsWorking(Subject signedOnUserSubject) {
        try {
            Class<?> mhn = Class.forName("org.apache.hadoop.security.User");
            Class[] argTypes = {String.class};
            Constructor<?> con = mhn.getDeclaredConstructor(argTypes);
            con.setAccessible(true);
            Object[] arguments = {""};
            Object instance = con.newInstance(arguments);
            signedOnUserSubject.getPrincipals().add((Principal)instance);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

