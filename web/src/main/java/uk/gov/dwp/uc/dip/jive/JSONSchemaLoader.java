package uk.gov.dwp.uc.dip.jive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.vaadin.data.Item;
import com.vaadin.ui.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.web.resources.ExceptionHandler;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by chrisrozacki on 07/02/2017.
 */
public class JSONSchemaLoader extends Button implements Button.ClickListener{

    private final static Logger log = Logger.getLogger(Upload.class);
    private final static ConsoleLogger consoleLogger = (ConsoleLogger) Logger.getLogger("ConsoleLogger", new Log4JFactory(JiveUI.Console));
    private Tree schemaTree;

    public JSONSchemaLoader(String caption,Tree menu){
        super(caption);
        addClickListener(this);
        schemaTree = menu;
    }

    public void buttonClick(Button.ClickEvent event){
        //Properties.getInstance().getSchemaLocation();
        //loadSchemaLocal("/Users/chrisrozacki/DIP/jive/agent_core_agenttodo.schema");
        String path = Properties.getInstance().getSchemaLocation();
        path= "hdfs:///user/chrisrozacki/meta-data/schema/etl/uc/mongo/2017-03-08/accepted-data/carerCircumstances/schema.json";
        String jsonSchema = getSchemaFromHDFSAsString(path);
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
        JsonNode typeNode = typeStruct.path("type");
        JsonNode fieldsNode = typeStruct.path("fields");

        if (fieldsNode.getNodeType() == JsonNodeType.ARRAY) {
            //iterate all elements
            fieldsNode.elements().forEachRemaining(e -> {

                String menuItemText = String.format("%s, type:%s, nullable:%s", e.path("name").asText()
                        , e.path("type").getNodeType() != JsonNodeType.NULL ? e.path("type").asText() : e.path("type").getNodeType()
                        , e.path("nullable").asText());

                Object id = schemaTree.addItem();
                schemaTree.setItemCaption(id, menuItemText);
                if (parentId != null) {
                    schemaTree.setParent(id,parentId );
                }

                JsonNode childType = e.path("type");
                if(childType.getNodeType() == JsonNodeType.OBJECT){
                    addItems(id, childType);
                }
            });
            return;
        }

        String menuItemText = String.format("%s, type:%s, nullable:%s", typeStruct.path("name").asText()
                , typeStruct.getNodeType() == JsonNodeType.STRING ? "struct" : typeStruct.asText()
                , typeStruct.path("nullable").asText());

        Object id = schemaTree.addItem();
        schemaTree.setItemCaption(id, menuItemText);
        if (parentId != null) {
            schemaTree.setParent(id,parentId);
        }
    }

    String getSchemaFromHDFSAsString(String path){
        final String TargetFile = "/tmp/schema.json";
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS","hdfs://192.168.99.100:8020");
        conf.set("dfs.client.socket-timeout", "5000");
        conf.set("dfs.client.use.datanode.hostname","true");
        conf.set("dfs.datanode.use.datanode.hostname","true");
        //conf.set("fs.defaultFS","hdfs://sandbox.hortonworks.com:8020");
        //conf.set("fs.defaultFS","hdfs://127.0.0.1:8020");

        FileSystem fs = null;
        try{
            fs = FileSystem.get(conf);
        }catch(Exception e) {
            consoleLogger.debug("error while getting file " + path + " " + e.toString());
            return null;
        }

        try {
            FileStatus status = fs.getFileStatus(new Path(path));
            consoleLogger.debug(status.getOwner());
        }
        catch(Exception e){
            log.debug(e.getMessage());
            consoleLogger.debug("error while storing json schema " + path + " " + e.toString());
            return null;
        }

        try {
            fs.copyToLocalFile(new Path(path), new Path(TargetFile));
        }catch(Exception e){
            log.debug(e.getMessage());
            consoleLogger.debug("error while storing json schema " + path + " " + e.toString());
            return null;
        }

        consoleLogger.debug("schema file fetched from " + path +" to " + TargetFile);

        try {
            return readFile(TargetFile, StandardCharsets.UTF_8);
        }
        catch(Exception e){
            consoleLogger.debug("error while storing json schema " + path + " " + e.toString());
            return null;
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
