package uk.gov.dwp.uc.dip.mappingreader;

import org.apache.commons.lang.builder.HashCodeBuilder;
import uk.gov.dwp.uc.dip.schemagenerator.datachecks.DataCheckEnum;
import java.util.*;
import static uk.gov.dwp.uc.dip.mappingreader.MappingTypeEnum.SOURCE_TYPE_CUSTOM;

/**
 * Helper class to represent a rule/row in the technical mapping
 */
public class TechnicalMapping {

    int sourceFileLineNo;
    String sourceDatabase;
    public String sourceCollection;
    public MappingTypeEnum targetType;
    public String targetFieldName;
    public String targetTableName;

    public String jsonPath;
    //todo: always use STRING as source type
    public MappingTypeEnum sourceType;
    public String format;
    public String function="";

    /**
     * List of checks as key=value, separated by comma
     */
    public String dataChecksString ="";

    private String userDefinedSourceType;
    private String userDefinedTargetType;

    @Override
    public String toString() {
        return String.format("%s.%s.%s(%s) - %s.%s(%s)",
                sourceDatabase, sourceCollection, jsonPath, sourceType, targetTableName, targetFieldName, targetType);
    }

    public void setUserDefinedSourceType(String userDefinedSourceType) {
        this.userDefinedSourceType = userDefinedSourceType.toUpperCase();
    }

    public void setUserDefinedTargetType(String userDefinedTargetType) {
        this.userDefinedTargetType = userDefinedTargetType.toUpperCase();
    }

    public String getSourceType(){
        if(sourceType == SOURCE_TYPE_CUSTOM){
            return userDefinedSourceType;
        }else{
            return sourceType.getHiveType();
        }
    }

    public String getTargetType(){
        if(targetType == SOURCE_TYPE_CUSTOM){
            return userDefinedTargetType;
        }else{
            return targetType.getHiveType();
        }
    }

    public String getPostgresType(){
        if(targetType == SOURCE_TYPE_CUSTOM){
            return userDefinedTargetType;
        }else{
            return targetType.getPostgresType();
        }
    }

    /***
     * Objects are equal when have the same source (db and collection) and the same target: db, column and type.
     * This sort of equality is used when grouping technical mapping rows together to coalesce them into one target column.
     * @param o - technical mapping role
     * @return
     */
    @Override
    public boolean equals(Object o){
        if(o==null) return false;
        TechnicalMapping rule = (TechnicalMapping) o;
        return sourceDatabase.equals(rule.sourceDatabase) &&
                sourceCollection.equals(rule.sourceCollection) &&
                targetType.equals(rule.targetType) &&
                targetFieldName.equals(rule.targetFieldName) &&
                targetTableName.equals(rule.targetTableName);
    }

    /***
     * Objects have the same hashcode when have the same source (db and collection) and the same target: db, column and type.
     * This sort of equality is used when grouping technical mapping rows together to coalesce them into one target column.
     * @return
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(sourceDatabase).
                        append(sourceCollection).
                        append(targetType).
                        append(targetFieldName).
                        append(targetTableName).
                        toHashCode();
    }

    /**
     * Data checks are provided as space separated list
     * @return key value pair of data checks
     */
    public Set<DataCheckEnum> getDataChecks(){
        Set<DataCheckEnum> dataChecks = new LinkedHashSet<>();
        String[] names = this.dataChecksString.split(" ");
        for(String name : names) {
            dataChecks.add(DataCheckEnum.getByTypeName(name));
        }
        return dataChecks;
    }
}
