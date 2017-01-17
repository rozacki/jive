package uk.gov.dwp.uc.dip.schemagenerator.common;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;

import java.util.*;

public class JsonPathUtils {

    /**
     *
     * @return split filed path by '.'
     */
    static public List<String> getSegments(String jsonPath){
        return new ArrayList<>(Arrays.asList(jsonPath.split("\\.")));
    }

    /**
     * Works like substring: returns subpath starting from start segment
     * @param jsonPath
     * @param start
     * @return
     */
    static public String subJSONPath(String jsonPath, int start){
        List<String> segments = getSegments(jsonPath);
        String s="";
        for(int i=start;i<segments.size();++i){
            if(s.length()>0)
                s+=".";
            s+=segments.get(i);
            //getJsonSegmentInfo(segments.get(i));
        }
        return s;
    }

    /**
     * Finds first indexing operator that has [*], [mk], [mv] in the jsonpath and splitys into two parts
     * This function is useful when exploding array and maps hence we look only for [*], [mk], [mv]
     * @return
     * ...
     */
    public static PathSplitByIndexOperatorInfo splitPathByIndexOperator(String jsonPath){
        String leftPath = "";
        String rightPath = "";
        boolean foundIndexOperator = false;
        boolean isMapPath = false;
        boolean isMapKeyPath = false;

        List<String> fields = getSegments(jsonPath);
        for(String field: fields){
            if(!foundIndexOperator) {
                JsonSegmentInfo jsonSegmentInfo = getJsonSegmentInfo(field);

                if (leftPath.length() > 0)
                    leftPath=leftPath.concat(".");
                leftPath=leftPath.concat(jsonSegmentInfo.normalizedJsonSegment);

                if ((jsonSegmentInfo.isArray && jsonSegmentInfo.arrayIndex == -1) || jsonSegmentInfo.isMap) {
                    foundIndexOperator = true;
                    isMapPath = jsonSegmentInfo.isMap;
                    isMapKeyPath = jsonSegmentInfo.isMapKey;
                }
            }else {
                if (rightPath.length() > 0)
                    rightPath = rightPath.concat(".");
                rightPath = rightPath.concat(field);
            }

        }
        return new PathSplitByIndexOperatorInfo(jsonPath, foundIndexOperator,leftPath, rightPath, isMapPath, isMapKeyPath);
    }

    /**
     * Adds back ticks to each segment of jsonPath or alone field name
     * @param path A JSON path
     * @return Path with back ticks around each element
     */
    static public String addBackTicks(String path){
        String retVal="";
        List<String> segments = getSegments(path);
        for(String segment: segments){
            JsonSegmentInfo jsonSegmentInfo = getJsonSegmentInfo(segment);
            // is it array?
            // todo: what if it is map
            if(jsonSegmentInfo.isArray){
                segment=segment.replaceAll("^","`").replaceAll("\\[","`\\[");
            }else{
                segment=segment.replaceAll("^","`").replaceAll("$","`");
            }
            if(retVal.length()>0){
                retVal+=".";
            }
            retVal+=segment;
        }
        return retVal;
    }

    /**
     * Static function to parse json segment and check if [..] is at the end.
     * @param segment is a part of jsonpath
     * @return json segment info
     */
    static JsonSegmentInfo getJsonSegmentInfo(String segment){
        JsonSegmentInfo jsonSegmentInfo = new JsonSegmentInfo();
        jsonSegmentInfo.Segment = segment;
        String[] parts = segment.split("\\[");
        if(parts.length > 1){
            int startIdx = segment.indexOf("[");
            int stopIdx = segment.indexOf("]");
            String fieldName = parts[0];
            int arrayIndex;

            String indexString = segment.substring(startIdx + 1, stopIdx);

            jsonSegmentInfo.isMapKey = indexString.equals("mk");
            jsonSegmentInfo.isMapValue = indexString.equals("mv");

            if(jsonSegmentInfo.isMapKey || jsonSegmentInfo.isMapValue){
                jsonSegmentInfo.isMap = true;
                jsonSegmentInfo.normalizedJsonSegment = fieldName;
                return jsonSegmentInfo;
            }

            if(indexString.equals("*")) {
                arrayIndex=-1;
            }else {
                arrayIndex = Integer.parseInt(indexString);
            }

            jsonSegmentInfo.isArray = true;
            jsonSegmentInfo.normalizedJsonSegment = fieldName;
            jsonSegmentInfo.arrayIndex = arrayIndex;
            return jsonSegmentInfo;
        }
        jsonSegmentInfo.normalizedJsonSegment = segment;
        return jsonSegmentInfo;
    }

    /**
     * 1. Sorts technical mapping based on jsonpath
     * 2. Groups technical mapping based on jsonpath segments
     * @param rules
     * @return
     */
    static public Map<String,List<TechnicalMapping>> groupByJSONPath(List<TechnicalMapping> rules){
        Map<String,List<TechnicalMapping>> groups = new LinkedHashMap<>();

        // must be sorted ASC before grouping
        rules.sort(new Comparator<TechnicalMapping>() {
            public int compare(TechnicalMapping t1, TechnicalMapping t2){
                return compareJSONPathsAsc(t1.jsonPath,t2.jsonPath);
            }
        });

        // group together by json path
        while(rules.size()>0){
            // create group with single jsonpath as group key and single element in it
            TechnicalMapping currentMapping = rules.get(0);
            rules.remove(0);
            groups.put(currentMapping.jsonPath,new ArrayList<TechnicalMapping>());
            groups.get(currentMapping.jsonPath).add(currentMapping);

            //iterate all and check
            int i=0;
            while(i<rules.size()){
                if(isSuperpath(currentMapping.jsonPath,rules.get(i).jsonPath)){
                    groups.get(currentMapping.jsonPath).add(rules.get(i));
                    rules.remove(i);
                }else{
                    i++;
                }
            }
        }

        return groups;
    }/**
     * Compares two jsonpath, starting with length, if length is the same then compares types:
     * array is always bigger than simpe type.
     * @param jsonPath1
     * @param jsonPath2
     * @return
     */
    static int compareJSONPathsAsc(String jsonPath1, String jsonPath2){
        List<String> segments1 =    getSegments(jsonPath1);
        List<String> segments2 =    getSegments(jsonPath2);

        if(segments1.size() == segments2.size()){
            for(int i=0;i<segments1.size();++i){
                JsonSegmentInfo segmentInfo1 = getJsonSegmentInfo(segments1.get(i));
                JsonSegmentInfo segmentInfo2 = getJsonSegmentInfo(segments2.get(i));

                if(segmentInfo1.isArray && !segmentInfo2.isArray){
                    return 1;
                }

                if(!segmentInfo1.isArray && segmentInfo2.isArray){
                    return -1;
                }
            }
            return 0;
        }

        if(segments1.size()>segments2.size()){
            return 1;
        }return -1;
    }

    static public int compareJSONPathsDesc(String jsonPath1, String jsonPath2){
        List<String> segments1 =    getSegments(jsonPath1);
        List<String> segments2 =    getSegments(jsonPath2);

        if(segments1.size() == segments2.size()){
            for(int i=0;i<segments1.size();++i){
                JsonSegmentInfo segmentInfo1 = getJsonSegmentInfo(segments1.get(i));
                JsonSegmentInfo segmentInfo2 = getJsonSegmentInfo(segments2.get(i));

                if(segmentInfo1.isArray && !segmentInfo2.isArray){
                    return -1;
                }

                if(!segmentInfo1.isArray && segmentInfo2.isArray){
                    return 1;
                }
            }
            return 0;
        }

        if(segments1.size()>segments2.size()){
            return -1;
        }return 1;
    }

    /**
     * Return TRUE if first jsonpath is isSuperpath for the second for example: a.b contains a.b.c.
     * First jsonpath must not be longer than the second one.
     * @param jsonPath1
     * @param jsonPath2
     * @return
     */
    static boolean isSuperpath(String jsonPath1, String jsonPath2){
        List<String> segments1 =    getSegments(jsonPath1);
        List<String> segments2 =    getSegments(jsonPath2);

        for(int i=0;i<segments1.size();++i){
            String segement1 = segments1.get(i), segment2 = segments2.get(i);
            JsonSegmentInfo jsonPathInfo1 = getJsonSegmentInfo(segement1), jsonPathInfo2 = getJsonSegmentInfo(segment2);

            if(!jsonPathInfo1.normalizedJsonSegment.equals(jsonPathInfo2.normalizedJsonSegment)){
                return false;
            }
        }
        return true;
    }
}
