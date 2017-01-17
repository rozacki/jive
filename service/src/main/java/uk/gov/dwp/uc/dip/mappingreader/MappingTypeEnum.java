package uk.gov.dwp.uc.dip.mappingreader;

public enum MappingTypeEnum {
    SOURCE_TYPE_STRING("string", "STRING", "TEXT"),
    SOURCE_TYPE_BOOL("boolean", "BOOLEAN", "BOOLEAN"),
    SOURCE_TYPE_OBJECT("object", "object", "object"),
    SOURCE_TYPE_INT("int", "INT", "INT"),
    SOURCE_TYPE_ARRAY("array", "array", "array"),
    SOURCE_TYPE_MAP("map", "map", "map"),
    SOURCE_TYPE_DATE("date", "date", "date"),
    SOURCE_TYPE_TIMESTAMP("timestamp", "timestamp", "timestamp"),
    SOURCE_TYPE_DOUBLE("double", "DOUBLE", "DOUBLE"),
    SOURCE_TYPE_CUSTOM("","","");

    private final String typeName;
    private final String hiveType;
    private final String postgresType;

    MappingTypeEnum(String typeName, String hiveType, String postgresType) {
        this.typeName = typeName;
        this.hiveType = hiveType;
        this.postgresType = postgresType;
    }

    public static MappingTypeEnum getByTypeName(String typeName){
        for (MappingTypeEnum type : MappingTypeEnum.values()){
            if(type.typeName.equalsIgnoreCase(typeName)){
                return type;
            }
        }

        return SOURCE_TYPE_CUSTOM;
    }

    public String getHiveType() {
        return hiveType;
    }

    public String getPostgresType() {
        return postgresType;
    }
}
