package uk.gov.dwp.uc.dip.schemagenerator.common;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;

import java.util.List;

/** This is the root document mapping schema keeping list of top fields schemas. These fields keep list of their children
 */
public class TechnicalMappingJSONSchema extends TechnicalMappingJSONFieldSchema {

    static public TechnicalMappingJSONSchema Start(){
        return new TechnicalMappingJSONSchema();
    }

    public TechnicalMappingJSONFieldSchema push(TechnicalMapping rule){

        List<String> fields = JsonPathUtils.getSegments(rule.jsonPath);

        return push(fields,rule, false);
    }
}
