package uk.gov.dwp.uc.dip.jive;

import com.google.common.io.Resources;
import com.google.common.primitives.Booleans;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import org.apache.ambari.view.ViewContext;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;

public class Properties {

    private final static Logger log = Logger.getLogger(Properties.class);

    private enum Property{
        WORKING_DIR("jive.temp.folder"),
        JSON_PATH("jive.json.path"),
        HIVE_HOST("jive.hive.host"),
        HIVE_PORT("jive.hive.port"),
        HIVE_PRINCIPAL_USER("jive.hive.principal.user"),
        JAAS_CONF("jive.jaas.conf.file"),
        HOST_REALM("jive.hive.host.realm"),
        NON_AMBARI_USER("jive.dev.user"),
        HIVE_AUTH_DISABLED("jive.hive.auth.disable"),
        SCHEMA_LOCATION("jive.schema.location")
        ;

        private String key;

        Property(String key) {

            this.key = key;
        }
    }

    private static Properties properties;
    private String dataLocation;
    private String uploadPath;
    private String scriptsPath;
    private String hiveHost;
    private String hivePrincipalUser;
    private String hivePort;
    private String jaasConfFile;
    private String hiveHostRealm;
    private String jiveDevUser;
    private boolean hiveAuthDisabled;
    private String schemaLocation;

    // TODO get properties from ambari OR properties file
    // TODO Perhaps lose the ambari properties completely.

    public static Properties getInstance(){
        if(null == properties){
            properties = new Properties();
            try {
                properties.read();
            } catch (IOException e) {
                log.error("Error reading properties file.",  e);
                Notification.show(e.getLocalizedMessage(), ERROR_MESSAGE);
            }
        }
        return properties;
    }

    private void read() throws IOException {

        ServletContext servletContext = VaadinServlet.getCurrent().getServletContext();
        ViewContext context = (ViewContext) servletContext.getAttribute(ViewContext.CONTEXT_ATTRIBUTE);
        Map<String,String> properties;

        if(null != context){
            properties = context.getProperties();
        }else{
            java.util.Properties p = new java.util.Properties();
            FileInputStream fis = new FileInputStream(
                    Resources.getResource("jive.properties").getPath());
            p.load(fis);

            properties = new HashMap<>();
            for(Map.Entry<Object, Object> entry : p.entrySet()){

                properties.put((String)entry.getKey(), (String)entry.getValue());
            }
        }
        dataLocation = checkPath(properties.get(Property.JSON_PATH.key));
        uploadPath = checkPath(properties.get(Property.WORKING_DIR.key)) + "/uploads/";
        scriptsPath = checkPath(properties.get(Property.WORKING_DIR.key)) + "/scripts/";
        hivePrincipalUser = properties.get(Property.HIVE_PRINCIPAL_USER.key);
        hiveHost = properties.get(Property.HIVE_HOST.key);
        hivePort = properties.get(Property.HIVE_PORT.key);
        jaasConfFile = properties.get(Property.JAAS_CONF.key);
        log.debug(jaasConfFile);
        hiveHostRealm = properties.get(Property.HOST_REALM.key);
        jiveDevUser = properties.get(Property.NON_AMBARI_USER.key);
        hiveAuthDisabled = Boolean.valueOf(properties.get(Property.HIVE_AUTH_DISABLED.key));
        schemaLocation = properties.get(Property.SCHEMA_LOCATION);
        checkDirectoriesExist();
    }

    private void checkDirectoriesExist() {
        File f = new File(getUploadPath());
        if(!f.exists()){
            f.mkdirs();
        }
        f = new File(getScriptsPath());
        if(!f.exists()){
            f.mkdirs();
        }
    }

    public String getDataLocation() {
        return dataLocation;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public String getScriptsPath() {
        return scriptsPath;
    }

    public String getHiveHost() {
        return hiveHost;
    }

    public String getHivePrincipalUser() {
        return hivePrincipalUser;
    }

    public String getHivePort() {
        return hivePort;
    }

    public String getJaasConfFile() {
        return jaasConfFile;
    }

    public String getHiveHostRealm() {
        return hiveHostRealm;
    }

    public String getJiveDevUser() {return jiveDevUser;}

    public boolean isHiveAuthenticationDisabled() {return hiveAuthDisabled;}

    public String getSchemaLocation() {return schemaLocation;}

    private String checkPath(String path){
        // Make sure all paths end with a forward slash
        String result = path;
        if(!path.endsWith("/")){
            result = result + "/";
        }
        return result;
    }
}
