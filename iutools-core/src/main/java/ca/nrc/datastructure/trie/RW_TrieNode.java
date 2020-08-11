package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

/**************************************
 * Serialize/deserialize a TrieNode to/from JSON
 * 
 * @author desilets
 *
 */
public class RW_TrieNode {
	
	private ObjectWriter nodeWriter = null;
	private ObjectMapper nodeMapper = null; 
	
	@JsonIgnore
	private ObjectMapper getNodeMapper() {
		if (nodeMapper == null) {
			nodeMapper = new ObjectMapper();
			nodeMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		}
		return nodeMapper;
	}

	@JsonIgnore
	private ObjectWriter getNodeWriter() {
		if (nodeWriter == null) 
		{
			// We don't write 'children' attribute to JSON because it should
			// be deduced from the set of subdirectories in a node file's 
			// parent directory.
			//
			SimpleBeanPropertyFilter propsFilter = SimpleBeanPropertyFilter
				.serializeAllExcept("children");
			FilterProvider filters = new SimpleFilterProvider()
				      .addFilter("TrieNodeFilter", propsFilter);
			nodeWriter = getNodeMapper().writer(filters);
		}
		
		return nodeWriter;
	}

	public void writeNode(File nodeFile, TrieNode node)
			throws RW_TrieNodeException {
		try {
			getNodeWriter().writeValue(nodeFile, node);
		} catch (IOException e) {
			throw new RW_TrieNodeException(e);
		}
	}

	public void writeValue(File file, Object value) throws RW_TrieNodeException {
		try {
			getNodeWriter().writeValue(file, value);
		} catch (IOException e) {
			throw new RW_TrieNodeException(e);
		}
	}

	public TrieNode readNode(File nodeFile) throws RW_TrieNodeException {
		TrieNode node = null;
		try {
			node = getNodeMapper().readValue(nodeFile, TrieNode.class);
		} catch (IOException e) {
			throw new RW_TrieNodeException(e);
		}
		return node;
	}

	public <T extends Object> T readValue(File file, Class<T> clazz) throws RW_TrieNodeException {
		T value = null;
		try {
			value = getNodeMapper().readValue(file, clazz);
		} catch (IOException e) {
			throw new RW_TrieNodeException(e);
		}
		return value;
	}
}
