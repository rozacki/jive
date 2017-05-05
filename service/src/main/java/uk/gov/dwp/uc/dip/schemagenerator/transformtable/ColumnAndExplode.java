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

    //column that we will select
    String column;
    //if exploding array or map then we generate this alias. It is used to select alias.columns
    String explodeAlias;
    String normalizedJson;

    // as of columns may require many explosions
    // it keeps pairs of <explodedAlias, normalizedJson>
    List<Map.Entry<String,String>> explodedAliases = new ArrayList<>();

    PathSplitByIndexOperatorInfo pathSplitByIndexOperatorInfo;

    ColumnAndExplode(String column, String explodeAlias, String normalizedJson
            , PathSplitByIndexOperatorInfo pathSplitByIndexOperator){
        this.column = column;
        this.explodeAlias = explodeAlias;
        this.normalizedJson = normalizedJson;
        this.pathSplitByIndexOperatorInfo = pathSplitByIndexOperator;
        this.explodedAliases.add(new AbstractMap.SimpleEntry<>(explodeAlias,normalizedJson));
    }

    String getColumnName(){
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
                        ,this.pathSplitByIndexOperatorInfo);
    }

    /*
       * Check each segment of technical mapping object jsonpath, if it's an array or map and check if it's
       * exploitable: array[*], map[mk], map[mv].
       * @param rule
       * @return if array[*] is present then we return select (First) and explode (Second)
       * if it's not present then we return jsonpath (First) and null (Second)
       */
    public static ColumnAndExplode findFirstColumnAndExplode(TechnicalMapping rule){
        String column;
        PathSplitByIndexOperatorInfo splitPathByExplodeOperator = JsonPathUtils.findFirstExplodeOperator(rule.jsonPath);

        // do we have split array
        if(!splitPathByExplodeOperator.exploitable)
            return new ColumnAndExplode(rule.jsonPath, null, null, splitPathByExplodeOperator);

        // create explode alias that will be used in SELECT by concatenating json path from before [] operator
        String explodeAlias = String.format("exploded_%s", TechnicalMappingJSONFieldSchema.normalizeHIVEObjectName(splitPathByExplodeOperator.leftJsonPath));
        if(splitPathByExplodeOperator.rightJsonPath.length()>0) {
            // if right part (after []) exists then we use it for column name in SELECT
            column = explodeAlias.concat(".").concat(splitPathByExplodeOperator.rightJsonPath);
        }else{
            // if right part (after[]) does not exists then we just use alias to create column name in SELECT
            column = explodeAlias;
        }
        return new ColumnAndExplode(column, explodeAlias, splitPathByExplodeOperator.leftJsonPath, splitPathByExplodeOperator);
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
            // column, exploded, normalized json,...
            return new ColumnAndExplode(rule.jsonPath, null, null, splitPathByExplodeOperator);
        }

        return null;

    }
}