package uk.gov.dwp.uc.dip.jive;

import org.apache.ambari.view.ViewContext;

import javax.inject.Inject;

class Properties {

    private static final String  JIVE_WORKING_DIRECTORY = "jive.folder";
    private static Properties properties;
    private String dataLocation;
    private String uploadPath = "/tmp/uploads/";
    private String scriptsPath = "/tmp/scripts/";
    private String hiveHost = "10.88.253.128";
    private String hiveUser = "paulroberts";
    private String hivePassword = "763514";
    private String hiveDatabase = "uc_dev_paulroberts";
    private String hivePort = "10000";

    // TODO get properties from ambari OR properties file
    // TODO Perhaps lose the ambari properties completely.

    @Inject
    ViewContext context;

    public static Properties getInstance(){
        if(null == properties){
            properties = new Properties();
            properties.read();
        }
        return properties;
    }

    // TODO refreshing values?
    private void read() {
        if(null != context){
            dataLocation = context.getProperties().get(JIVE_WORKING_DIRECTORY);
        }else{
            dataLocation = "/etl/uc/mongo/";
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

    public String getHiveUser() {
        return hiveUser;
    }

    public String getHivePassword() {
        return hivePassword;
    }

    public String getHiveDatabase() {
        return hiveDatabase;
    }

    public String getHivePort() {
        return hivePort;
    }

}
