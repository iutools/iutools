package ca.nrc.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TypePreservingMapperTest {

    /////////////////////////////////////
    // DOCUMENTATION TESTS
    /////////////////////////////////////

    @Test
    public void test__TypePreservingMapper__Synopsis() throws Exception {
        // Say you have a collection of elements that are of different
        // types
        //
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("float_prop", new Float(1E02));
        props.put("strarr_prop", new String[] {"hello", "world"});

        // If you serialize props with a default ObjectMapper(), the mapping
        // will loose information about the specific type of the elements
        //
        ObjectMapper defaultMapper = new ObjectMapper();
        String json = defaultMapper.writeValueAsString(props);

        // If you try to retrieve the elements into a variable of the
        // type of the value that was put into props, you may
        // get an exception
        //
        Map<String,Object> readProps = defaultMapper.readValue(json, props.getClass());
        try {
            String[] readStringArray = (String[]) readProps.get("strarr_prop");
            Assert.fail("You will never get to this point because an exception will be raised.");
        } catch (ClassCastException e) {
            // Nothing to do... we expect the exception to be raised
        }

        // But with a TypePreservingMapper, you can do this without getting
        // an exception
        //
        TypePreservingMapper tpMapper = new TypePreservingMapper();
        json = tpMapper.writeValueAsString(props);
        readProps = tpMapper.readValue(json, props.getClass());
        String[] readStringArray = (String[]) readProps.get("strarr_prop");
    }
}
