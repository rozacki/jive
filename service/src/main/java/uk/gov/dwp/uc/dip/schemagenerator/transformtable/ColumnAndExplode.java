package uk.gov.dwp.uc.dip.schemagenerator.transformtable;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.schemagenerator.common.JsonPathUtils;
import uk.gov.dwp.uc.dip.schemagenerator.common.PathSplitByIndexOperatorInfo;
import uk.gov.dwp.uc.dip.schemagenerator.common.TechnicalMappingJSONFieldSchema;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ColumnAndExplode {
    final static String REMOVED = "_removed.";

    //column for SELECT column, can be the same as wxplode if we dealing with array[*] simple type
    String column;
    //if exploding array or map then we generate this alias. It is used to generate SELECT alias.columns or SELECT alias
    String explodeAlias;
    // used to generate LATERAL VIEW OUTER EXPLODE(normalizedJson)
    String normalizedJson;

    // we need list columns may require many explosions
    // it keeps pairs of <explodedAlias, normalizedJson>
    List<Map.Entry<String,String>> explodedAliases = new ArrayList<>();

    // need it to generate LATERAL VIEW OUTER EXPLODE(%s) view_%s AS %s_key, %s_value
    // maps can only have one level of exploding
    boolean isMap;

    ColumnAndExplode(String column, String explodeAlias, String normalizedJson, Boolean isMap){
        this.column = column;
        this.explodeAlias = explodeAlias;
        this.normalizedJson = normalizedJson;
        this.isMap = isMap;
        this.explodedAliases.add(new AbstractMap.SimpleEntry<>(explodeAlias,normalizedJson));
    }

    private static String getColumnName(String column, PathSplitByIndexOperatorInfo pathSplitByIndexOperatorInfo){
        if(pathSplitByIndexOperatorInfo.isMapPath){
            if(pathSplitByIndexOperatorInfo.isMapKeyPath){
                return String.format("%s_key", column);
            }else{
                return String.format("%s_value", column);
            }
        }
        return column;
    }

    public ColumnAndExplode getRemovedVersion(){
        return
                new ColumnAndExplode(this.column.replace(this.explodeAlias, this.explodeAlias + "Removed")
                        ,this.explodeAlias + "Removed"
                        ,REMOVED + this.normalizedJson
                        ,this.isMap);
    }

    String createRemovedColumn(){
        if(null == this.explodeAlias){
            return REMOVED + this.column;
        }else{
            // when exploding removed array we have to give different name than for not removed array hence +'Removed'
            return this.column.replace(this.explodeAlias, this.explodeAlias + "Removed");
        }
    }
    /*
       * Check each segment of technical mapping object jsonpath, if it's an array or map and check if it's
       * exploitable: array[*], map[mk], map[mv].
       * @param rule
       * @return if array[*] is present then we return select (First) and explode (Second)
       * if it's not present then we return jsonpath (First) and null (Second)
       */
    public static ColumnAndExplode findFirstColumnAndExplode(String jsonPath){
        String column;
        PathSplitByIndexOperatorInfo splitPathByExplodeOperator = JsonPathUtils.findFirstExplodeOperator(jsonPath);

        if(!splitPathByExplodeOperator.exploitable)
            return new ColumnAndExplode(jsonPath, null, null, splitPathByExplodeOperator.isMapPath);

        // create explode alias that will be used in SELECT by concatenating json path from before [] operator
        String explodeAlias = String.format("exploded_%s", TechnicalMappingJSONFieldSchema.normalizeHIVEObjectName(splitPathByExplodeOperator.leftJsonPath));
        if(splitPathByExplodeOperator.rightJsonPath.length()>0) {
            // if right part (after []) exists then we use it for column name in SELECT
            // column = exploded_array.field
            column = explodeAlias.concat(".").concat(splitPathByExplodeOperator.rightJsonPath);
        }else{
            // if right part (after[]) does not exist then we just use alias to create column name in SELECT
            // column = exploded_array
            column = explodeAlias;
        }
        // column, exploded, normalized json,...
        return new ColumnAndExplode( getColumnName(column,splitPathByExplodeOperator), explodeAlias, splitPathByExplodeOperator.leftJsonPath, splitPathByExplodeOperator.isMapPath);
    }

    /*
       * Check each segment of technical mapping object jsonpath, if it's an array or map and check if it's
       * exploitable: array[*], map[mk], map[mv].
       * @param rule
       * @return if array[*] is present then we return select (First) and explode (Second)
       * if it's not present then we return jsonpath (First) and null (Second)
       */
    private ColumnAndExplode findColumnAndExplodes(TechnicalMapping rule){

        PathSplitByIndexOperatorInfo splitPathByExplodeOperator = JsonPathUtils.findFirstExplodeOperator(rule.jsonPath);

        //
        if(!splitPathByExplodeOperator.exploitable){
            // nothing to explode, column, exploded, normalized json,...
            return new ColumnAndExplode(rule.jsonPath, null, null, splitPathByExplodeOperator.isMapPath);
        }
        if(splitPathByExplodeOperator.rightJsonPath.length()>0){

        }

        return null;

    }
}