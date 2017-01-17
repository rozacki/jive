package uk.gov.dwp.uc.dip.schemagenerator.transformtable;

import uk.gov.dwp.uc.dip.schemagenerator.common.PathSplitByIndexOperatorInfo;

class SelectAndExplode{

    //column that we will select
    String selectColumn;
    //if exploding array or map then we generate this alias. It is used to select alias.columns
    String explodeAlias;
    String normalizedJson;

    PathSplitByIndexOperatorInfo pathSplitByIndexOperatorInfo;

    SelectAndExplode(String selectSQL, String explodeAlias, String normalizedJson
            , PathSplitByIndexOperatorInfo pathSplitByIndexOperator){
        this.selectColumn = selectSQL;
        this.explodeAlias = explodeAlias;
        this.normalizedJson = normalizedJson;
        this.pathSplitByIndexOperatorInfo = pathSplitByIndexOperator;
    }

    String getSelectColumnsName(){
        if(pathSplitByIndexOperatorInfo.isMapPath){
            if(pathSplitByIndexOperatorInfo.isMapKeyPath){
                return String.format("%s_key",selectColumn);
            }else{
                return String.format("%s_value",selectColumn);
            }
        }
        return  selectColumn;
    }
}