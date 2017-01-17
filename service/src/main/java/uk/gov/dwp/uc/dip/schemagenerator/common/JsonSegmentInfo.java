package uk.gov.dwp.uc.dip.schemagenerator.common;

class JsonSegmentInfo {

    String Segment;

    boolean isArray;
    boolean isMap;

    /**
     * It's a field stripped off from index operator
     */
    String normalizedJsonSegment = "";

    // Array specific
    /**
     * Arrays index, map key if it's int
     */
    int arrayIndex =-1;

    // Map specific
    /** if map key has been found then mapKeyType is the type of the key
     *
     */
    boolean isMapKey;

    /** if map value has been found then mapKeyType is the type of the key
     *
     */
    boolean isMapValue;
}
