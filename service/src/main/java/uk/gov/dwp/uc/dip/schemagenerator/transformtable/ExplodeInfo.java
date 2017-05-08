package uk.gov.dwp.uc.dip.schemagenerator.transformtable;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.schemagenerator.common.JsonPathUtils;
import uk.gov.dwp.uc.dip.schemagenerator.common.PathSplitByIndexOperatorInfo;
import uk.gov.dwp.uc.dip.schemagenerator.common.TechnicalMappingJSONFieldSchema;

import java.util.*;

class ExplodeInfo {
    final static String REMOVED = "_removed.";

    //column for SELECT column, can be the same as wxplode if we dealing with array[*] simple type
    String column;

    //if exploding array or map then we generate this alias. It is used to generate SELECT alias.columns or SELECT alias
    // used to generate LATERAL VIEW OUTER EXPLODE(explodePath)
    // we need list columns may require many explosions
    // it keeps pairs of <alias , path>
    HashMap<String,String> explodeAliases = new LinkedHashMap<>();

    // need it to generate LATERAL VIEW OUTER EXPLODE(%s) view_%s AS %s_key, %s_value
    // maps can only have one level of exploding
    boolean isMap;

    // This constructor is used for array of arrays hence many exlodes
    ExplodeInfo(){
    }

    // to do obsolete
    ExplodeInfo(String column, String explodeAlias, String explodePath, Boolean isMap){
        this.column = column;
        this.isMap = isMap;
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

    ExplodeInfo getRemovedVersionOfExplodeAlias2(){
        ExplodeInfo removeExplodeInfo = new ExplodeInfo();
        String lastAlias = "";
        String path = "";
        boolean firstAlias = false;

        // for example system fields
        if(this.explodeAliases.size() == 0){
            removeExplodeInfo.column = "_removed." + this.column;
            return removeExplodeInfo;
        }
        // explodeAliases has to be sorted ascending
        for(String alias: this.explodeAliases.keySet()){

            if(!firstAlias) {
                path = "_removed." + this.explodeAliases.get(alias);
                lastAlias = "removed_" + alias;
                firstAlias = true;
            }
            else {
                path = "removed_" + this.explodeAliases.get(alias);
                lastAlias = "removed_" + alias;
            }

            removeExplodeInfo.explodeAliases.put(lastAlias, path);
        }
        removeExplodeInfo.column = "removed_" + this.column;

        return removeExplodeInfo;
    }

    /*
       * Check each segment of technical mapping object jsonpath, if it's an array or map and check if it's
       * exploitable: array[*], map[mk], map[mv].
       * @param rule
       * @return if array[*] is present then we return select (First) and explode (Second)
       * if it's not present then we return jsonpath (First) and null (Second)
       */
    static ExplodeInfo createExplodeInfo(TechnicalMapping rule){
        //
        PathSplitByIndexOperatorInfo splitPathByExplodeOperator = JsonPathUtils.findFirstExplodeOperator(rule.jsonPath);

        //
        if(!splitPathByExplodeOperator.exploitable){
            // nothing to explode: column, alias, path, not map
            return new ExplodeInfo(rule.jsonPath, null, null, false);
        }
        ExplodeInfo explodeInfo = new ExplodeInfo();

        String alias = "";
        String path = "";

        while(true){
            // create explode lateral view alias that will be used in SELECT
            if(alias.length()>0) {
                alias += "_";
                path +=".";
            }
            alias += String.format("exploded_%s"
                    , TechnicalMappingJSONFieldSchema.normalizeHIVEObjectName(splitPathByExplodeOperator.leftJsonPath));
            path += splitPathByExplodeOperator.leftJsonPath;

            // remember current alias and path and drill down
            explodeInfo.explodeAliases.put(alias, path);

            if(splitPathByExplodeOperator.rightJsonPath.length()>0){
                PathSplitByIndexOperatorInfo splitPathByExplodeOperator2 =
                        JsonPathUtils.findFirstExplodeOperator(splitPathByExplodeOperator.rightJsonPath);

                // if nothing to explode then we will wrap-up
                if(!splitPathByExplodeOperator2.exploitable){
                    explodeInfo.column = alias + "." + splitPathByExplodeOperator.rightJsonPath;
                    return explodeInfo;
                }

                path = alias;
                splitPathByExplodeOperator = splitPathByExplodeOperator2;

            }else{
                // nothing left on the right side of jsonpath
                // store column and finish
                // we may be dealing with map
                explodeInfo.column = getColumnName(alias, splitPathByExplodeOperator);
                explodeInfo.isMap = splitPathByExplodeOperator.isMapPath;

                return explodeInfo;
            }

        }
    }
}