package uk.gov.dwp.uc.dip.schemagenerator.common;

public class PathSplitByIndexOperatorInfo {
    // map[..] and array[*] are exploitable
    public boolean exploitable;
    public String leftJsonPath;
    public String rightJsonPath;
    public boolean isMapPath;
    public boolean isMapKeyPath;
    public String JSONPath;

    PathSplitByIndexOperatorInfo(String JSONPath,
                                 boolean indexOperatorFound,
                                 String leftJsonPath,
                                 String rightJsonPath,
                                 boolean isMap,
                                 boolean isMapKey){
        this.JSONPath = JSONPath;
        this.exploitable = indexOperatorFound;
        this.leftJsonPath = leftJsonPath;
        this.rightJsonPath = rightJsonPath;
        this.isMapPath = isMap;
        this.isMapKeyPath = isMapKey;
    }
}
