package uk.gov.dwp.uc.dip.schemagenerator.common;

public class PathSplitByIndexOperatorInfo {
    public boolean indexOperatorFound;
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
        this.indexOperatorFound = indexOperatorFound;
        this.leftJsonPath = leftJsonPath;
        this.rightJsonPath = rightJsonPath;
        this.isMapPath = isMap;
        this.isMapKeyPath = isMapKey;
    }
}
