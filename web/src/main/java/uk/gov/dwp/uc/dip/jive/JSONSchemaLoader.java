package uk.gov.dwp.uc.dip.jive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.vaadin.data.Item;
import com.vaadin.ui.*;
import org.apache.log4j.Logger;

import java.io.File;
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
        loadSchemaLocal("/Users/chrisrozacki/DIP/jive/agent_core_agenttodo.schema");
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
}
