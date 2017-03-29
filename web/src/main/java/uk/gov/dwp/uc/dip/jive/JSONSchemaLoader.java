package uk.gov.dwp.uc.dip.jive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.vaadin.ui.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import uk.gov.dwp.uc.dip.jive.schema.*;

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
        log.debug(path);
        String jsonSchema = getSchemaFromFSAsString(path);
        loadSchema(jsonSchema);
    }

    void loadSchema(String jsonSchema){
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
                        String menuItemText = String.format("%s, type: array, nullable:%s"
                                , e.path("name").asText()
                                , e.path("nullable").asText()
                        );

                        try {
                            SparkJsonSchemaType schemaType = new SparkJsonSchemaType(
                                    e.path("name").asText()
                                    , "array", e.path("nullable").asText());
                        }catch(Exception ex){
                            return;
                        }

                        Object id = addTreeItem(parentId, menuItemText);
                        addItems(id, elementTypeNode);
                    }else{
                        String menuItemText = String.format("%s, type: array, nullable:%s, elementType:%s, containsNull:%s, elementType:%s"
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

    String getSchemaFromFSAsString(String path){
        final String TargetFile = "/tmp/" + schemaFileName;
        Configuration conf = new Configuration();

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

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
