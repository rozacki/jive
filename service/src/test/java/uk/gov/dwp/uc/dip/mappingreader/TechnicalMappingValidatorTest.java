package uk.gov.dwp.uc.dip.mappingreader;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class TechnicalMappingValidatorTest {

    private TechnicalMappingValidator validator;

    private String getResourcePath(String testFileName){
        return Resources.getResource(this.getClass(), testFileName).getPath();
    }

    private TechnicalMappingReader getReader(String testFileName) throws IOException {
        return TechnicalMappingReader.getInstance(getResourcePath(testFileName));
    }

    private TechnicalMappingReader getReaderAndRead(String testFileName) throws IOException {
        TechnicalMappingReader reader = getReader(testFileName);
        reader.read();
        return reader;
    }

    @Before
    public void setUp() throws Exception {
        validator = new TechnicalMappingValidator();
    }

    @Test
    public void isFileAccessible() throws Exception {
        assertFalse("Non existent file test.", validator.isFileAccessible("notafile"));
        assertTrue(validator.getErrors().size() == 1);
    }

    @Test
    public void isFileTypeOk() throws Exception {
        assertTrue(validator.isFileTypeOk("\\a\\b\\afile.csv"));
        assertTrue(validator.isFileTypeOk("\\a\\b\\afile.xlsx"));
        assertTrue(validator.getErrors().size() == 0);
        assertFalse(validator.isFileTypeOk("\\a\\b\\afile.bob"));
        assertTrue(validator.getErrors().size() == 1);
    }

    @Test
    public void isNumberOfColumnsOk_false() throws Exception {
        assertFalse("Source file column count not ok", validator.isNumberOfColumnsOk(getReader("not_enough_columns.csv")));
        assertEquals("Source file column count not ok", 1,validator.getErrors().size());
    }

    @Test
    public void isNumberOfColumnsOk_true() throws Exception {
        assertTrue("Source file column count ok", validator.isNumberOfColumnsOk(getReader("correct_number_columns.csv")));
        assertEquals("Source file column count ok", 0,validator.getErrors().size());
    }

    @Test
    public void isDestinationDataTypeOk() throws Exception {
        assertTrue("Valid data type.", validator.isDestinationDataTypeOk(getReaderAndRead("valid_destination_data_types.csv")));
        assertEquals("Valid data type.", 0, validator.getErrors().size());
        assertFalse("Invalid data type.", validator.isDestinationDataTypeOk(getReaderAndRead("invalid_destination_data_types.csv")));
        assertEquals("Invalid data type.", 1, validator.getErrors().size());
    }

    @Test
    public void isArrayPathValid() throws Exception {
        assertTrue("Valid array path.", validator.isArrayPathValid(getReaderAndRead("valid_array_paths.csv")));
        assertEquals("Valid array path.", 0, validator.getErrors().size());
        assertFalse("Invalid array path.", validator.isArrayPathValid(getReaderAndRead("invalid_array_paths.csv")));
        assertEquals("Invalid array path.", 1, validator.getErrors().size());
    }

    @Test
    public void isMapTypeValid() throws Exception {
        assertTrue("Valid key/value map test.", validator.isMapTypeValid(getReaderAndRead("valid_key_value_map.csv")));
        assertEquals("Valid key/value map test.", 0, validator.getErrors().size());
        assertFalse("Invalid key/value map test.", validator.isMapTypeValid(getReaderAndRead("invalid_key_value_map.csv")));
        assertEquals("Invalid key/value map test.", 2, validator.getErrors().size());
    }

    @Test
    public void isJsonPathValid() throws Exception {
        assertTrue("Valid json path.", validator.isJsonPathValid(getReaderAndRead("valid_json_path.csv")));
        assertEquals("Valid json path.", 0, validator.getErrors().size());
        assertFalse("Invalid json path.", validator.isJsonPathValid(getReaderAndRead("invalid_json_path.csv")));
        assertEquals("Invalid json path.", 2 , validator.getErrors().size());
    }

    @Test
    public void isFileValid() throws Exception {
        assertTrue("Whole file valid.", validator.isFileValid(getResourcePath("valid_json_path.csv")));
        assertEquals("Whole file valid.", 0, validator.getErrors().size());
    }

}