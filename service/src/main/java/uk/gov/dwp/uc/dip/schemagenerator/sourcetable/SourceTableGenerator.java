package uk.gov.dwp.uc.dip.schemagenerator.sourcetable;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingReader;
import uk.gov.dwp.uc.dip.schemagenerator.common.JsonPathUtils;
import uk.gov.dwp.uc.dip.schemagenerator.common.TechnicalMappingJSONFieldSchema;
import uk.gov.dwp.uc.dip.schemagenerator.common.TechnicalMappingJSONSchema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 12/12/16.
 * Generate SQL to wrap a JSON collection to make a HIVE external table.
 */

public class SourceTableGenerator {
    private String sourceJsonFileLocation;
    private String targetSourceTableName;

    public SourceTableGenerator(String sourceJsonFileLocation) {

        this.sourceJsonFileLocation = sourceJsonFileLocation;
    }

    public List<String> generateSqlForTable(TechnicalMappingReader techMap, String targetTable, String sourceDatabase, String sourceCollection) {
        List<String> result = new ArrayList<>();
        List<TechnicalMapping> sourceRules = techMap.getSourceFields(targetTable
                ,sourceDatabase, sourceCollection);

        targetSourceTableName = TechnicalMappingJSONFieldSchema.normalizeHIVEObjectName(
                String.format("src_%s_%s_%s",sourceDatabase, sourceCollection, targetTable));

        String sourceLocation = String.format("%s/%s/%s", sourceJsonFileLocation, sourceDatabase, sourceCollection);

        // iterate all rules and create schema
        TechnicalMappingJSONSchema schema = TechnicalMappingJSONSchema.Start();
        // must be sorted ASC before grouping
        sourceRules.sort(new Comparator<TechnicalMapping>() {
            public int compare(TechnicalMapping t1, TechnicalMapping t2){
                return JsonPathUtils.compareJSONPathsDesc(t1.jsonPath,t2.jsonPath);
            }
        });
        for(TechnicalMapping rule: sourceRules) {
            schema.push(rule);
        }

        result.add(String.format("DROP TABLE IF EXISTS %s\n",targetSourceTableName));

        // create and output schema
        result.add(createSourceTableSQL(schema, targetSourceTableName, sourceLocation));

        return result;
    }

    private String createSourceTableSQL(TechnicalMappingJSONSchema schema, String targetTable, String dataLocation){
        String columnDefinitions="";

        for(HashMap.Entry<String,TechnicalMappingJSONFieldSchema> entry: schema.children.entrySet()){
            String columnDef = getSourceColumnSQL(entry.getValue(), false);
            if(columnDefinitions.length()>0){
                columnDefinitions+=",";
            }
            columnDefinitions+=columnDef+"\n";
        }

        String columnDefinitionsRemoved="";

        for(HashMap.Entry<String,TechnicalMappingJSONFieldSchema> entry: schema.children.entrySet()){
            String columnDef = getSourceColumnSQL(entry.getValue(), true);
            if(columnDefinitionsRemoved.length()>0){
                columnDefinitionsRemoved+=",";
            }
            columnDefinitionsRemoved+=columnDef+"\n";
        }

        return String.format(
                "CREATE EXTERNAL TABLE %s(" +
                        "\n%s" +
                        ",`_removed` STRUCT<%s" +
                        ">)" +
                        "\nROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe'" +
                        "\nSTORED AS TEXTFILE" +
                        "\nLOCATION '%s'", targetTable
                ,columnDefinitions, columnDefinitionsRemoved, dataLocation);
    }

    /*
     * Generates source for column based on field schema information.
     *
     * @param fieldSchema
     * @return SQL string
     *
     */
    private String getSourceColumnSQL(TechnicalMappingJSONFieldSchema fieldSchema, boolean forceColonAsFieldTypeSeparator){
        String nameTypeSeparator = ":";

        // field - sourceType separators vary depending on the level
        if(!fieldSchema.isChild && !forceColonAsFieldTypeSeparator)
            nameTypeSeparator = " ";

        switch (fieldSchema.type) {
            case SOURCE_TYPE_ARRAY:
                return getArrayColumnDefinition(fieldSchema, nameTypeSeparator);
            case SOURCE_TYPE_MAP:
                // support for MAP<simple_type, simple_type>
                return getMapColumnDefinition(fieldSchema, nameTypeSeparator);
            case SOURCE_TYPE_OBJECT:
                return getObjectColumnDefinition(fieldSchema, nameTypeSeparator);
            case SOURCE_TYPE_CUSTOM:
                return getUserColumnDefinition(fieldSchema, nameTypeSeparator);
            default:
                return getSimpleColumnDefinition(fieldSchema, nameTypeSeparator);
        }
    }

    // todo: it does not support array of array[1][2]
    private String getArrayColumnDefinition(TechnicalMappingJSONFieldSchema fieldSchema, String nameTypeSeparator){
        String columnDefinitions = "";

        // is this array of simple types?
        if (fieldSchema.arraySimpleType != null) {
            // does it have also structure inside? if it does it is just to call a function on this map
            // so we can ignore sourcing it as it will be anyway with the struct inside
            // todo: add special indicator: function to the schema instead of inferring from the size
            return String.format("`%s`%sARRAY<%s>"
                        , fieldSchema.fieldName, nameTypeSeparator, fieldSchema.arraySimpleType.type.getHiveType());
        }

        // is this array of structs?
        for (HashMap.Entry<String, TechnicalMappingJSONFieldSchema> entry : fieldSchema.children.entrySet()) {
            String columnDef = getSourceColumnSQL(entry.getValue(), true);
            if (columnDefinitions.length() > 0) {
                columnDefinitions += ",";
            }
            columnDefinitions += columnDef + "\n";
        }

        return String.format("`%s`%sARRAY<STRUCT<%s>>", fieldSchema.fieldName, nameTypeSeparator, columnDefinitions);
    }

    private String getMapColumnDefinition(TechnicalMappingJSONFieldSchema fieldSchema, String nameTypeSeparator){
        return String.format("`%s`%sMAP<%s,%s>",
                fieldSchema.fieldName, nameTypeSeparator
                , fieldSchema.mapKeyType.getHiveType(), fieldSchema.mapValueType.getHiveType());
    }

    private String getObjectColumnDefinition(TechnicalMappingJSONFieldSchema fieldSchema, String nameTypeSeparator){
        String columnDefinitions = "";
        for (HashMap.Entry<String, TechnicalMappingJSONFieldSchema> entry : fieldSchema.children.entrySet()) {
            String columnDef = getSourceColumnSQL(entry.getValue(), false);
            if (columnDefinitions.length() > 0) {
                columnDefinitions += ",";
            }
            columnDefinitions += columnDef + "\n";
        }
        columnDefinitions = columnDefinitions.substring(0, columnDefinitions.length()-1);
        return String.format("`%s`%sSTRUCT<%s>", fieldSchema.fieldName, nameTypeSeparator, columnDefinitions);
    }

    private String getSimpleColumnDefinition(TechnicalMappingJSONFieldSchema fieldSchema, String nameTypeSeparator){
        return String.format("`%s`%s%s",
                fieldSchema.fieldName, nameTypeSeparator, fieldSchema.type.getHiveType());
    }

    private String getUserColumnDefinition(TechnicalMappingJSONFieldSchema fieldSchema, String nameTypeSeparator){
        return String.format("`%s`%s%s",
                fieldSchema.fieldName, nameTypeSeparator, fieldSchema.userDefinedType);
    }

    public String getTargetSourceTableName() {
        return targetSourceTableName;
    }
}
