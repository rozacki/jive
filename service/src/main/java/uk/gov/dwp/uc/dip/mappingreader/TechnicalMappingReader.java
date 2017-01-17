package uk.gov.dwp.uc.dip.mappingreader;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class TechnicalMappingReader {
    public List<TechnicalMapping> rules = new LinkedList<>();

    String filePath;

    public abstract void read() throws IOException;
    abstract int getColumnCount();

    TechnicalMappingReader(String filePath) {
        this.filePath = filePath;
    }


    /**
     * Build a new technical mapping from a CSV file
     *
     * @param filePath tm file path definition
     * @throws IOException
     */
    public static TechnicalMappingReader getInstance(String filePath)
            throws IOException, TechnicalMappingException {
        TechnicalMappingValidator validator = new TechnicalMappingValidator();

        if(!validator.isFileAccessible(filePath) || !validator.isFileTypeOk(filePath)){
            throw new TechnicalMappingException(filePath + validator.getErrors());
        }

        String extension = FilenameUtils.getExtension(filePath);

        switch(extension) {
            case "xlsx":
                return new TechnicalMappingReaderXslx(filePath);
            case "csv":
                return new TechnicalMappingReaderCsv(filePath);
            default:
                throw new TechnicalMappingException("extension " + extension + " is not supported.");
        }

    }

    /**
     * Returns the set of unique fields used for a given collection.
     *
     * The fields are expressed like a json path in dotted format.
     *
     *   toplevelentry.nestedchild1.anothernestedchild2.whateverleafnode
     *
     * @param collection The name of the collection
     * @return The unique set of fields used for a collection
     */
    public Set<String> getUsedFieldsForCollection(String collection) {
        Set<String> res = new HashSet<>();
        for(TechnicalMapping o: rules) {
            if (o.sourceCollection.equals(collection)) {
                res.add(o.jsonPath);
            }
        }
        return res;
    }

    /**
     *
     * @return all databases that are used to transform into target table
     */
    public Set<String> getSourceDatabases(String targetTable){
        Set<String> res = new HashSet<>();
        for(TechnicalMapping rule: rules) {
            if(rule.targetTableName.equals(targetTable))
                res.add(rule.sourceDatabase);
        }
        return res;
    }


    /*
     *
     * @param targetTable
     * @param sourceDatabase
     * @return  @return all collections that are used to transform into target table from source database
     */
    public Set<String> getSourceCollection(String targetTable, String sourceDatabase){
        Set<String> res = new HashSet<>();
        for(TechnicalMapping rule: rules) {
            if(rule.targetTableName.equals(targetTable) && rule.sourceDatabase.equals(sourceDatabase))
                res.add(rule.sourceCollection);
        }
        return res;
    }

    /*
      Get a set of mappings for the columns in one target table (and source mongo db/collection
      // TODO just do this on target table?
     */
    public List<TechnicalMapping> getSourceFields(String targetTable, String sourceDatabase, String sourceCollection){
        List<TechnicalMapping> res =   new ArrayList<>();
        for(TechnicalMapping rule: rules) {
            if(rule.targetTableName.equals(targetTable) && rule.sourceDatabase.equals(sourceDatabase)
                    && rule.sourceCollection.equals(sourceCollection))
                res.add(rule);
        }
        return SystemColumns.updateOrAddSystemColumnMappings(res);
    }

    /**
     *
     * @return list of distinct target tables
     */
    public Set<String> getTargetTables(){
        Set<String> res = new HashSet<>();
        for(TechnicalMapping rule: rules) {
            res.add(rule.targetTableName);
        }
        return res;
    }

    /*
     * returns list of columns based on target table
     * @param table
     * @return based on target field name, list of unique target rules
     */
    public List<TechnicalMapping> getTargetColumns(String table){
        List<TechnicalMapping>  res =   new ArrayList<>();
        for(TechnicalMapping rule: rules) {
            if(rule.targetTableName.equals(table))
                res.add(rule);
        }
        return SystemColumns.updateOrAddSystemColumnMappings(res);
    }

    /***
     * Groups all technical rules together based on:
     * - target table and column name
     * - target type
     * - source database and source collection
     * @param rules
     * @return
     */
    static public HashMap<TechnicalMapping,List<TechnicalMapping>> groupByTarget(List<TechnicalMapping> rules){
        HashMap<TechnicalMapping,List<TechnicalMapping>> groups = new LinkedHashMap<>();

        for(TechnicalMapping rule: rules){
            if(!groups.containsKey(rule)){
                List<TechnicalMapping> group = new ArrayList<>();
                group.add(rule);
                groups.put(rule,group);
            }else{
                groups.get(rule).add(rule);
            }
        }

        return groups;
    }
}
