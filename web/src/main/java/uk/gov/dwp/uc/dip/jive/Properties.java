package uk.gov.dwp.uc.dip.jive;

import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import org.apache.ambari.view.ViewContext;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Map;

import static com.vaadin.ui.Notification.Type.ERROR_MESSAGE;

class Properties {

    private final static Logger log = Logger.getLogger(Properties.class);

    private enum Property{
        WORKING_DIR("jive.temp.folder"),
        JSON_PATH("jive.json.path"),
        HIVE_HOST("jive.hive.host"),
        HIVE_PORT("jive.hive.port"),
        HIVE_USER("jive.hive.user")
        ;

        private String key;

        Property(String key) {

            this.key = key;
        }
    }

    private static Properties properties;
    private String dataLocation;
    private String uploadPath = "/tmp/uploads/";
    private String scriptsPath = "/tmp/scripts/";
    private String hiveHost = "10.88.253.128";
    private String hiveUser = "paulroberts";
    private String hivePort = "10000";

    // TODO get properties from ambari OR properties file
    // TODO Perhaps lose the ambari properties completely.

    static Properties getInstance(){
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
            p.load(this.getClass().getResourceAsStream("jive.properties"));
            //noinspection unchecked
            properties = (Map<String, String>) p.entrySet();
        }
        dataLocation = checkPath(properties.get(Property.JSON_PATH.key));
        uploadPath = checkPath(properties.get(Property.WORKING_DIR.key)) + "/uploads/";
        scriptsPath = checkPath(properties.get(Property.WORKING_DIR.key)) + "/scripts/";
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

    public String getHiveUser() {
        return hiveUser;
    }

    public String getHivePort() {
        return hivePort;
    }

    private String checkPath(String path){
        // Make sure all paths end with a forward slash
        String result = path;
        if(!path.endsWith("/")){
            result = result + "/";
        }
        return result;
    }
}
