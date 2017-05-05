package uk.gov.dwp.uc.dip.schemagenerator.common;

public class PathSplitByIndexOperatorInfo {
    // map[..] and array[*] are exploitable
    public boolean exploitable;
    public String leftJsonPath;
    public String rightJsonPath;
    // are we dealing with map at all?
    public boolean isMapPath;
    // are we dealing with map and key path, if no then we looking at value
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
