package uk.gov.dwp.uc.dip.schemagenerator.transformtable;

import uk.gov.dwp.uc.dip.schemagenerator.common.PathSplitByIndexOperatorInfo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ColumnAndExplode {

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
}