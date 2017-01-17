package uk.gov.dwp.uc.dip.schemagenerator.postgrestable;


import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingReader;

import java.util.HashMap;
import java.util.List;

public class PostgresTableGenerator {
    public String getSqlForTable(TechnicalMappingReader techMap, String targetTable){
        // get all rules related to one database/collection
        List<TechnicalMapping> rules=techMap.getTargetColumns(targetTable);
        HashMap<TechnicalMapping, List<TechnicalMapping>> groupedRules = TechnicalMappingReader.groupByTarget(rules);
        String sqlFields="";
        /*
        for(TechnicalMapping entry: rules){
            if(sqlFields.length()>0)
                sqlFields+=",";
            sqlFields += String.format("%s %s\n", entry.targetFieldName, entry.getPostgresType());
        }
        */
        for(HashMap.Entry<TechnicalMapping,List<TechnicalMapping>> group: groupedRules.entrySet()){
            TechnicalMapping entry = group.getKey();
            if(sqlFields.length()>0)
                sqlFields+=",";
            sqlFields += String.format("%s %s\n", entry.targetFieldName, entry.getPostgresType());
        }
        /*
        for(TechnicalMapping entry: rules){
            if(sqlFields.length()>0)
                sqlFields+=",";
            sqlFields += String.format("%s %s\n", entry.targetFieldName, entry.getPostgresType());
        }
        */
        return String.format("drop table if exists land.%s;\ncreate table land.%s (%s);",targetTable, targetTable, sqlFields);
    }
}
