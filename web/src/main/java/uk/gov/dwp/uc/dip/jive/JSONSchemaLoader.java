package uk.gov.dwp.uc.dip.jive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.vaadin.ui.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;

/**
 * Created by chrisrozacki on 07/02/2017.
 */
public class JSONSchemaLoader extends Button implements Button.ClickListener{

    private final static Logger log = Logger.getLogger(Upload.class);
    private final static ConsoleLogger consoleLogger = (ConsoleLogger) Logger.getLogger("ConsoleLogger", new Log4JFactory(JiveUI.Console));
    private Tree schemaTree;
    private MappingEditor mappingEditor;
    final String schemaFileName = "schema.json";

    public JSONSchemaLoader(MappingEditor mappingEditor, String caption,Tree menu){
        super(caption);
        addClickListener(this);
        schemaTree = menu;
        this.mappingEditor = mappingEditor;
    }

    public void buttonClick(Button.ClickEvent event){
        String path = Properties.getInstance().getSchemaLocation();
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");

        path+= "/" + dt1.format(mappingEditor.dateTime.getValue()) + "/" + mappingEditor.databasesComboBox.getValue()
                + "/" + mappingEditor.collectionsComboBox.getValue() + "/" + schemaFileName;
        String jsonSchema;
        if(Properties.getInstance().isAuthenticationDisabled())
            jsonSchema = getSchemaFromFSAsString(path,false);
        else
            jsonSchema = getSchemaFromFSAsStringAuth(path);

        loadSchemaHDFS(jsonSchema);
    }

    void loadSchemaHDFS(String jsonSchema){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            log.debug(jsonSchema);
            rootNode = mapper.readTree(jsonSchema);
        }
        catch(Exception ex){
            consoleLogger.error("Error while reading json schema " + ex.getMessage());
            log.error(ex);
            return;
        }
        consoleLogger.info("JSON schema loaded");
        schemaTree.removeAllItems();
        addItems(null, rootNode);
    }

    void loadSchemaLocal(String path){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(new File(path));
        }
        catch(Exception ex){
            consoleLogger.error("Error while loading file "+ path);
            log.error(ex);
            return;
        }
        consoleLogger.info("JSON file loaded " + path);
        schemaTree.removeAllItems();
        addItems(null, rootNode);
    }

    /***
     * Todo support for arrayType
     * @param parentId
     * @param typeStruct
     */
    void addItems(Object parentId, JsonNode typeStruct){
        if (typeStruct.path("fields").getNodeType() == JsonNodeType.ARRAY) {
            JsonNode fieldsNode = typeStruct.path("fields");
            //iterate all elements
            fieldsNode.elements().forEachRemaining(e -> {

                if(getType(e).equals("array")){
                    JsonNode elementTypeNode = e.path("type").path("elementType");

                    if(elementTypeNode.getNodeType() == JsonNodeType.OBJECT) {
                        String menuItemText = String.format("%s, type:array, nullable:%s"
                                , e.path("name").asText()
                                , e.path("nullable").asText()
                        );
                        Object id = addTreeItem(parentId, menuItemText);
                        addItems(id, elementTypeNode);
                    }else{
                        String menuItemText = String.format("%s, type: array, elementType:%s, nullable:%s, containsNull:%s, elementType:%s"
                                , e.path("name").asText()
                                , getType(e)
                                , e.path("nullable").asText()
                                , e.path("type").path("containsNull").asText()
                                , e.path("type").path("elementType").asText());
                        Object id = addTreeItem(parentId, menuItemText);
                    }
                    return;
                }

                String menuItemText = String.format("%s, type:%s, nullable:%s"
                        , e.path("name").asText()
                        , e.path("type").asText()
                        , e.path("nullable").asText());

                Object id = addTreeItem(parentId, menuItemText);

                JsonNode childType = e.path("type");
                if(childType.getNodeType() == JsonNodeType.OBJECT){
                    addItems(id, childType);
                }
            });
            return;
        }

        if(typeStruct.path("type").equals("array")) {
            String menuItemText = String.format("elementType:%s, containsNull:%s"
                    , typeStruct.path("elementType").asText()
                    , typeStruct.path("containsNull").asText());

            addTreeItem(parentId, menuItemText);
        }else {

            String menuItemText = String.format("%s, type:%s, nullable:%s"
                    , typeStruct.path("name").asText()
                    , getType(typeStruct)
                    , typeStruct.path("nullable").asText());

            addTreeItem(parentId, menuItemText);
        }
    }

    /**
     * Helper, as adding new item to tree has a few steps
     * @param parentId
     * @param caption
     * @return
     */
    Object addTreeItem(Object parentId, String caption){
        Object id = schemaTree.addItem();
        schemaTree.setItemCaption(id, caption);
        if (parentId != null) {
            schemaTree.setParent(id,parentId );
        }
        return id;
    }

    /**
     * Helper, as "type" can be either simple type or complex
     * @param e
     * @return
     */
    String getType(JsonNode e){
        // if JsonNode does not exit e.path() will return JsonNodeType.NULL
        JsonNode typeNode = e.path("type");
        JsonNodeType typeNodeName = typeNode.getNodeType();

        switch(typeNodeName){
            // it's either object or array
            case OBJECT:
                return typeNode.path("type").asText();
            //it's a simple type
            case STRING:
            default:
                return e.asText();
        }
    }

    String getSchemaFromFSAsString(String path, boolean authEnabled){
        final String TargetFile = "/tmp/" + schemaFileName;
        Configuration conf = new Configuration();
        conf.set("dfs.client.socket-timeout", "5000");
        // tell namenode to return hostname instead of ip address
        conf.set("dfs.client.use.datanode.hostname","true");
        conf.set("dfs.datanode.use.datanode.hostname","true");

        if(!Properties.getInstance().getFSEndpoint().isEmpty())
            conf.set("fs.defaultFS",Properties.getInstance().getFSEndpoint());

        FileSystem fs = null;
        try{
            fs = FileSystem.get(conf);
        }catch(Exception e) {
            consoleLogger.debug("error while getting file " + path + " " + e.toString());
            log.debug(e.getMessage());
            log.debug(getStackTrace(e));
            return null;
        }

        try {
            fs.copyToLocalFile(new Path(path), new Path(TargetFile));
        }catch(Exception e){
            consoleLogger.debug("error while getting json schema " + path + " " + e.toString());
            log.debug(e.getMessage());
            log.debug(getStackTrace(e));
            return null;
        }

        consoleLogger.debug("schema file fetched from " + path +" to " + TargetFile);

        try {
            return readFile(TargetFile, StandardCharsets.UTF_8);
        }
        catch(Exception e){
            consoleLogger.debug("error while reading json schema to string " + path + " " + e.toString());
            log.debug(e.getMessage());
            log.debug(getStackTrace(e));
            return null;
        }
    }

    /**
     *
     * @param path
     * @return
     * This method does not work - hence we load schema from local file system
     */
    String getSchemaFromFSAsStringAuth(String path){
        log.debug("authentication enabled");
        Subject subject = null;

        try {
            subject = login();
        } catch (LoginException e) {
            NotificationUtils.displayError(e);
            log.error("Failed login (for proxy jdbc)", e);
            return null;
        }

        if(null != subject) {
            HackToGetSubjectDoAsWorking(subject);
            try {

                return (String) Subject.doAs(subject, new PrivilegedExceptionAction<String>() {
                    public String  run() throws Exception {
                        return getSchemaFromFSAsString(path,true);
                    }});
            } catch (Exception e) {
                NotificationUtils.displayError(e);
                log.error(e);
                return null;

            }
        }else{
            log.error("Null Kerberos subject");
            return null;
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
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

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
