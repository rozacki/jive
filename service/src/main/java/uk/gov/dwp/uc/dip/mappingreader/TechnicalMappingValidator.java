package uk.gov.dwp.uc.dip.mappingreader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class TechnicalMappingValidator {

    private List<String> errors = new ArrayList<>();

    @SuppressWarnings("unused")
    public boolean isFileValid(String filePath){
        errors = new ArrayList<>();
        TechnicalMappingReader reader;
        if(!isFileTypeOk(filePath) || !isFileAccessible(filePath)) return false;

        try {
            reader = TechnicalMappingReader.getInstance(filePath);
            reader.read();
            if(!isNumberOfColumnsOk(reader)
                    || !isDestinationDataTypeOk(reader)
                    || !isJsonPathValid(reader)
                    || !isArrayPathValid(reader)
                    || !isMapTypeValid(reader)) {
                return false;
            }
        } catch (IOException e) {
            // Should be no error as validation done for file io.
            e.printStackTrace();
            errors.add(e.getLocalizedMessage());
            return false;
        }

        return true;
    }

    boolean isFileAccessible(String filePath){
        File f = new File(filePath);
        if(!f.isFile()){
            errors.add("File does not exist: " + filePath);
            return false;
        }
        if(!f.canRead()){
            errors.add("File not readable, check permissions:" + filePath);
            return false;
        }
        return true;
    }

    boolean isFileTypeOk(String filePath){
        String extension = FilenameUtils.getExtension(filePath);

        switch(extension) {
            case "xlsx":
                return true;
            case "csv":
                return true;
            default:
                errors.add("extension " + extension + " is not supported.");
                return false;
        }

    }

    boolean isNumberOfColumnsOk(TechnicalMappingReader reader){

        if(reader.getColumnCount() == TechnicalMappingColumnsEnum.values().length){
            return true;
        }else{
            errors.add("Wrong number of columns in file.");
            return false;
        }
    }

    boolean isDestinationDataTypeOk(TechnicalMappingReader reader){
        String[] validTypes = {"tinyint","smallint","integer"
                ,"bigint", "float", "double", "decimal"
                ,"timestamp", "date", "string", "varchar", "char", "boolean"};
        boolean result = true;
        List<String> validTypeList = Arrays.asList(validTypes);
        for(TechnicalMapping tm : reader.rules){
            if(tm.targetType == MappingTypeEnum.SOURCE_TYPE_CUSTOM){
                if(!validTypeList.contains(tm.getTargetType().split("\\(")[0].toLowerCase())){
                    errors.add("Invalid target data type, line: "
                            + tm.sourceFileLineNo + ", type:" + tm.getTargetType());
                    result = false;
                }
            }
        }
        return result;
    }

    boolean isArrayPathValid(TechnicalMappingReader reader){
        // Only number or * between braces
        boolean result = true;
        for(TechnicalMapping tm : reader.rules){
            if(tm.jsonPath.contains("[")){
                String betweenBraces =
                        tm.jsonPath.substring(tm.jsonPath.indexOf("[") + 1
                                ,tm.jsonPath.indexOf("]"));
                if(!"*".equals(betweenBraces)
                        && !"mk".equals(betweenBraces)
                        && !"mv".equals(betweenBraces)){
                    if(!StringUtils.isNumeric(betweenBraces) || "".equals(betweenBraces)) {
                        errors.add("Invalid array mapping: " + tm.jsonPath
                                + " (line:" + tm.sourceFileLineNo + ")");
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    boolean isMapTypeValid(TechnicalMappingReader reader){
        // mv and mk in json for a destination table must come in pairs.
        Set<String> targetTables = reader.getTargetTables();
        boolean result = true;
        for(String table : targetTables){
            int keyCount = 0;
            int valueCount = 0;
            for(TechnicalMapping tm : reader.getTargetColumns(table)){
                if(tm.jsonPath.contains("[")) {
                    String betweenBraces =
                            tm.jsonPath.substring(tm.jsonPath.indexOf("[") + 1
                                    , tm.jsonPath.indexOf("]"));
                    if("mk".equals(betweenBraces)){
                        keyCount = keyCount + 1;
                    }
                    if("mv".equals(betweenBraces)){
                        valueCount = valueCount + 1;
                    }
                }
            }
            // If we have either key or value then should have both.
            if(keyCount > 0 || valueCount > 0){
                if(keyCount != 1 || valueCount != 1){
                    result = false;
                    errors.add("Table " + table + " has invalid key value mapping." +
                            " Should have One [mv] and One [mk] mapping.");
                }
            }
        }

        return result;
    }

    boolean isJsonPathValid(TechnicalMappingReader reader){
        boolean result = true;
        for(TechnicalMapping tm : reader.rules){
            if(tm.jsonPath.startsWith(".") || tm.jsonPath.endsWith(".")){
                errors.add("JSON path expected to not begin or end with full stop. Line: " + tm.sourceFileLineNo);
                result = false;
            }
        }
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public List<String> getErrors() {
        return errors;
    }
}
