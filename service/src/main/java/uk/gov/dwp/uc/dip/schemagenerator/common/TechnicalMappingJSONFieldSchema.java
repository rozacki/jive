package uk.gov.dwp.uc.dip.schemagenerator.common;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.mappingreader.MappingTypeEnum;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static uk.gov.dwp.uc.dip.mappingreader.MappingTypeEnum.SOURCE_TYPE_ARRAY;
import static uk.gov.dwp.uc.dip.mappingreader.MappingTypeEnum.SOURCE_TYPE_MAP;
import static uk.gov.dwp.uc.dip.mappingreader.MappingTypeEnum.SOURCE_TYPE_OBJECT;

/*
 * Created by chrisrozacki on 09/11/2016.
 */
public class TechnicalMappingJSONFieldSchema {
    /**
     * Field name is used as the fields key map and to generate SQL
     */
    public String fieldName;

    /**
     * Used to generate SQL. If type is complex then we expand children schema nodes.
     *
     *
     * Many sources can be used to generate one target. Type is used to facilitate it but order in TM is important.
     * If type is simple type but the schema node have children then the children will be omitted for the SOURCE part.
     * Instead, in the transform layer we will use get_json_object() to expand .
     * The type will have to be always STRING as get_son_object(String jsonString, String pathString)  accepts strings only.
     */
    public MappingTypeEnum type;
    public String userDefinedType;

    /**
§     * if sourceType is array holding simple sourceType
     */
    public TechnicalMappingJSONFieldSchema arraySimpleType = null;

    /**
     * important to know as syntax is slightly different when we are children for example:
     * field_name STRING -> field_name:STRING
     */
    public boolean isChild;

    /**
     *  If type is MAP then both mapKeyType is the key simple type and mapValueType is the simple value type
     *  Used to generate SQL
     */
    public MappingTypeEnum mapKeyType;
    public MappingTypeEnum mapValueType;

    /**
     * List of all children associated with this schema-node
     */
    public HashMap<String,TechnicalMappingJSONFieldSchema> children = new LinkedHashMap<>();

    TechnicalMappingJSONFieldSchema(){}

    private TechnicalMappingJSONFieldSchema(String fieldName, MappingTypeEnum type, boolean isChild){
        this.fieldName=fieldName;
        this.type = type;
        this.isChild = isChild;
    }

    /**
     * This constructor is used to create field schema based on TM object of the last field in chain
     * @param rule Rue to take sourceType from
     * @param isChild isChild
     */
    private TechnicalMappingJSONFieldSchema(TechnicalMapping rule, boolean isChild, String pathSegment){
        this.fieldName = pathSegment;
        this.type = rule.sourceType;
        this.isChild = isChild;
        this.userDefinedType = rule.getSourceType();
    }

    private static TechnicalMappingJSONFieldSchema AsArrayType(String fieldName, boolean isChild){
        return new TechnicalMappingJSONFieldSchema(fieldName, SOURCE_TYPE_ARRAY,isChild);
    }

    private static TechnicalMappingJSONFieldSchema AsObjectType(String fieldName, boolean isChild){
        return new TechnicalMappingJSONFieldSchema(fieldName, SOURCE_TYPE_OBJECT, isChild);
    }

    private static TechnicalMappingJSONFieldSchema AsMapType(String fieldName, boolean isKey, MappingTypeEnum type, boolean isChild){
        TechnicalMappingJSONFieldSchema schemaField = new TechnicalMappingJSONFieldSchema(fieldName, SOURCE_TYPE_MAP, isChild);
        if(isKey)
            schemaField.mapKeyType = type;
        else
            schemaField.mapValueType = type;

        return schemaField;
    }

    /*
     * Reverse engineer schema based on rules.
     * The client of this method creates the list of rules based on some criteria for example: source database and collection
     * In 'push' method, when a field is found then we check if its name already exists in the list. If yes then
     * we push. If not, then we create and push.
     *
     * MAP requires two rules (records in config) hence we allow the second one
     * CURRENT's SOLUTION DESIGN DOES NOT SUPPORT MAPS' VALUES AS STRUCTS
     * @return

     * @param fields
     * @param rule
     * @param isChild
     * @return
     */
    TechnicalMappingJSONFieldSchema push(List<String> fields, TechnicalMapping rule, boolean isChild){
        if(fields.size()==0){
            return this;
        }

        // check array and create key from field name by stripping '[?]'
        JsonSegmentInfo jsonSegmentInfo = JsonPathUtils.getJsonSegmentInfo(fields.get(0));

        if(jsonSegmentInfo.isMap && children.containsKey(jsonSegmentInfo.normalizedJsonSegment)){
            TechnicalMappingJSONFieldSchema mapSchema = children.get(jsonSegmentInfo.normalizedJsonSegment);

            if(jsonSegmentInfo.isMapKey){
                mapSchema.mapKeyType = rule.sourceType;
            }else{
                mapSchema.mapValueType = rule.sourceType;
            }
            // if we return here support only for MAP<simple_type,simple_type>
            return this;
        }

        //if such key DOES NOT exist then we have to add it
        if(!children.containsKey(jsonSegmentInfo.normalizedJsonSegment)) {

            //is it array
            if (jsonSegmentInfo.isArray) {
                TechnicalMappingJSONFieldSchema arraySchema = TechnicalMappingJSONFieldSchema.AsArrayType(jsonSegmentInfo.normalizedJsonSegment
                        , isChild);
                children.put(jsonSegmentInfo.normalizedJsonSegment, arraySchema);

                // if it is array of objects then we push
                if (fields.size() > 1) {
                    fields.remove(0);
                    return arraySchema.push(fields, rule, true);
                }

                // array holding simple types only so add this simple sourceType to array
                if (fields.size() == 1) {
                    arraySchema.arraySimpleType = new TechnicalMappingJSONFieldSchema(rule, true, fields.get(0));
                    return this;
                }
            }else if(jsonSegmentInfo.isMap) {
                // create MAP
                TechnicalMappingJSONFieldSchema mapSchema = AsMapType(jsonSegmentInfo.normalizedJsonSegment,jsonSegmentInfo.isMapKey
                        , rule.sourceType, isChild);
                // put MAP
                children.put(jsonSegmentInfo.normalizedJsonSegment, mapSchema);
                return mapSchema;
            //it must be struct
            }else if(fields.size()>1){
                TechnicalMappingJSONFieldSchema objectSchema =
                        TechnicalMappingJSONFieldSchema.AsObjectType(fields.get(0),isChild);
                children.put(fields.get(0), objectSchema);
                fields.remove(0);
                return objectSchema.push(fields, rule, true);
            //just single field
            }else{
                // it must be simple source type
                TechnicalMappingJSONFieldSchema fieldSchema = new TechnicalMappingJSONFieldSchema(rule, isChild, fields.get(0));
                children.put(fields.get(0),fieldSchema);
            }
            return this;
        }
        // get schema field and push
        fields.remove(0);
        return children.get(jsonSegmentInfo.normalizedJsonSegment).push(fields,rule, true);
    }

    /*
     * Replaces all invalid characters and turn into lower case. Is there a method in HIVE lib that does it?
     * @param in what
     * @return
     */
    static public String normalizeHIVEObjectName(String in){
        return in.toLowerCase().replaceAll("[-\\.\\[\\]\\*\\s]","_");
    }
}
