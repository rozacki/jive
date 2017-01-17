package uk.gov.dwp.uc.dip.mappingreader;

import java.util.List;

/**
 * Created by paul on 30/11/16.
 * Add system standard mappings if not specified in user defined mapping.
 */
class SystemColumns {

    static List<TechnicalMapping> updateOrAddSystemColumnMappings(List<TechnicalMapping> rulesForTable){
        if(rulesForTable.size()==0)
            return rulesForTable;
        // Get some common values
        TechnicalMapping existingRule = rulesForTable.get(0);

        // Create and add system rules
        for(SystemColumnsEnum systemColumnsEnum : SystemColumnsEnum.values()){
            if (!mappingExistsForColumn(rulesForTable, systemColumnsEnum.destinationFieldName)) {

                TechnicalMapping tm = new TechnicalMapping();
                tm.sourceDatabase = existingRule.sourceDatabase;
                tm.sourceCollection = existingRule.sourceCollection;
                tm.targetTableName = existingRule.targetTableName;
                tm.targetFieldName = systemColumnsEnum.destinationFieldName;
                tm.jsonPath = systemColumnsEnum.sourceFieldLocation;
                tm.sourceType = MappingTypeEnum.getByTypeName(systemColumnsEnum.sourceDataType);
                tm.targetType = MappingTypeEnum.getByTypeName(systemColumnsEnum.destinationDataType);
                tm.function = "";
                rulesForTable.add(tm);
            }
        }

        return rulesForTable;
    }

    private static boolean mappingExistsForColumn(List<TechnicalMapping> rules, String targetFieldName){
        for(TechnicalMapping rule : rules){
            String existingTarget = removeLeadingUnderscore(rule.targetFieldName);
            String newTarget = removeLeadingUnderscore(targetFieldName);
            if(newTarget.equalsIgnoreCase(existingTarget)) return true;
        }
        return false;
    }

    private static String removeLeadingUnderscore(String in){
        if (in.startsWith("_")){
            return in.substring(1, in.length());
        }
        return in;
    }
}
