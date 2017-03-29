package uk.gov.dwp.uc.dip.jive.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrisrozacki on 22/03/2017.
 */
public class SparkJsonSchemaType {
    public String Name;
    public String Type;
    public String Nullability;
    public List<SparkJsonSchemaType> Fields = new ArrayList<>();
    /**
     * Indicates if root has beedn found
     */
    static boolean RootFound = false;

    private final static Logger log = Logger.getLogger(SparkJsonSchemaType.class);

    public static SparkJsonSchemaType create(String jsonSchema){
        RootFound = false;
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(jsonSchema);
            return  createFromJsonNode(null, rootNode);
        }
        catch(Exception ex){
            log.error(ex);
            return null;
        }
    }

    /**
     *
     * @param currentNode
     * @return
     */
    static SparkJsonSchemaType createFromJsonNode(SparkJsonSchemaType parent, JsonNode currentNode){
        String name="root",type="";

        try {
            SparkJsonSchemaType schemaType = new SparkJsonSchemaType(currentNode.path("name").asText(), currentNode.path("type").asText(),"");
            JsonNode fieldsNode = currentNode.path("fields");
            if(fieldsNode.getNodeType() == JsonNodeType.NULL){

            }
            return schemaType;
        }catch (Exception ex){
            log.debug(ex.getMessage());
            return null;
        }
    }

    public SparkJsonSchemaType(String name, String type, String nullability) throws Exception{
        if(name.isEmpty()) {
            if (RootFound) {
                throw new Exception("invalid type");
            }
            else {
                this.Name = "root";
                RootFound = true;
            }
        }else{
            this.Name = name;
        }

        if(type.isEmpty())
            throw new Exception("invalid type");

        this.Type = type;

    }

    @Override
    public String toString(){
        return this.toString();
    }

}
