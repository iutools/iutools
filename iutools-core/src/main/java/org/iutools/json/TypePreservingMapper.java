package org.iutools.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.io.File;
import java.io.IOException;

/**
 * Map object to/from json while prerving information about the type
 * of elements in collections.
 */
public class TypePreservingMapper  {

    private ObjectMapper _mapper = null;
    private ObjectWriter _writer = null;

    // List of properties that should not be written to JSON
    String[] ignoreProps = new String[0];
    String propsFilterID = "none";


    public TypePreservingMapper() throws TypePreservingMapperException {
        init_TypePreservingMapper(null);
    }

    public TypePreservingMapper(String... _ignoreProps) throws TypePreservingMapperException {
        init_TypePreservingMapper(_ignoreProps);
    }

    private void init_TypePreservingMapper(String[] _ignoreProps) throws TypePreservingMapperException {
        if (_ignoreProps != null) {
            ignoreProps = _ignoreProps;
            propsFilterID = "filter "+String.join(",", _ignoreProps);
        }
    }

    private ObjectMapper mapper() {
        if (_mapper == null) {
            _mapper = new ObjectMapper();
            _mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        }
        return _mapper;
    }

    private ObjectWriter writer() {
        if (_writer == null)
        {
            // We don't write 'children' attribute to JSON because it should
            // be deduced from the set of subdirectories in a node file's
            // parent directory.
            //
            SimpleBeanPropertyFilter propsFilter = SimpleBeanPropertyFilter
                    .serializeAllExcept(ignoreProps);
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter(propsFilterID, propsFilter);
            _writer = mapper().writer(filters);
        }

        return _writer;
    }

    public <T extends Object> T readValue(String json, Class<T> aClass) throws TypePreservingMapperException {
        T obj = null;
        try {
            obj = mapper().readValue(json, aClass);
        } catch (IOException e) {
            throw new TypePreservingMapperException(e);
        }
        return obj;
    }

    public <T extends Object> T readValue(File file, Class<T> clazz) throws TypePreservingMapperException {
        T obj = null;
        try {
            obj = mapper().readValue(file, clazz);
        } catch (IOException e) {
            throw new TypePreservingMapperException(e);
        }

        return obj;
    }


    public void writeValue(File file, Object obj) throws TypePreservingMapperException {
        try {
            writer().writeValue(file, obj);
        } catch (IOException e) {
            throw new TypePreservingMapperException(e);
        }
    }

    public String writeValueAsString(Object obj) throws TypePreservingMapperException {
        String json = null;
        try {
            json = writer().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new TypePreservingMapperException(e);
        }
        return json;
    }

}
