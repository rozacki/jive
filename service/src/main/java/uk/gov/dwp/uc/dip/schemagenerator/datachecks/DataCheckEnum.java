package uk.gov.dwp.uc.dip.schemagenerator.datachecks;

/**
 * Created by chrisrozacki on 13/01/2017.
 */
public enum DataCheckEnum {
    UNIQUE_DATA_CHECK("unique"),
    NOT_NULLABLE("not_nullable"),
    CUSTOM("");

    private final String typeName;

    DataCheckEnum(String name){
        typeName = name;
    }

    /** Converts string to DataCheckEnum, if no match found then returns CUSTOM DataCheckEnum
     */
    public static DataCheckEnum getByTypeName(String typeName){
        for (DataCheckEnum type : DataCheckEnum.values()){
            if(type.typeName.equalsIgnoreCase(typeName)){
                return type;
            }
        }

        return CUSTOM;
    }
}
