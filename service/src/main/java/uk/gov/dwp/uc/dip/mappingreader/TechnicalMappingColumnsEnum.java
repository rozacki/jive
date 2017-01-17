package uk.gov.dwp.uc.dip.mappingreader;

/**
 * Columns in spreadsheet or csv file.
 */
public enum TechnicalMappingColumnsEnum {
    SOURCE_DATABASE(0),
    SOURCE_COLLECTION(1),
    SOURCE_PATH(2),
    SOURCE_DATA_TYPE(3),
    TARGET_TABLE(4),
    TARGET_FIELD(5),
    TARGET_DATA_TYPE(6),
    FUNCTION(7),
    TARGET_CONSTRAINTS(8);

    int columnNumber;
    TechnicalMappingColumnsEnum(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
