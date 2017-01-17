package uk.gov.dwp.uc.dip.mappingreader;

/**
 * Created by paul on 30/11/16.
 * Holds a list of system column mappings to apply to all tables
 */
enum SystemColumnsEnum {
    VERSION("_version", "string", "version", "int")
    ,CREATED_DATE_TIME("createdDateTime.d_date", "string", "created_ts", "timestamp")
    ,MODIFIED_DATE_TIME("_lastModifiedDateTime.d_date", "string", "last_modified_ts","timestamp")
    ,REMOVED_DATE_TIME("_removedDateTime.d_date", "string", "removed_ts", "timestamp");

    String sourceFieldLocation;
    String sourceDataType;
    String destinationFieldName;
    String destinationDataType;

    SystemColumnsEnum(String sourceFieldLocation, String sourceDataType, String destinationFieldName, String destinationDataType) {
        this.sourceFieldLocation = sourceFieldLocation;
        this.sourceDataType = sourceDataType;
        this.destinationFieldName = destinationFieldName;
        this.destinationDataType = destinationDataType;
    }
}
