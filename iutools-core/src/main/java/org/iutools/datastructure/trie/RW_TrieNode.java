package org.iutools.datastructure.trie;

import java.io.File;

import org.iutools.json.TypePreservingMapper;
import org.iutools.json.TypePreservingMapperException;

/**************************************
 * Serialize/deserialize a TrieNode to/from JSON
 * 
 * @author desilets
 *
 */
public class RW_TrieNode {
	
//	private ObjectWriter nodeWriter = null;
//	private ObjectMapper nodeMapper = null;

	private TypePreservingMapper _mapper = null;

	TypePreservingMapper mapper() throws RW_TrieNodeException {
		if (_mapper == null) {
			try {
				_mapper = new TypePreservingMapper();
			} catch (TypePreservingMapperException e) {
				throw new RW_TrieNodeException(e);
			}
		}
		return _mapper;
	}

	public void writeNode(File nodeFile, TrieNode node)
			throws RW_TrieNodeException {
		try {
			mapper().writeValue(nodeFile, node);
		} catch (TypePreservingMapperException e) {
			throw new RW_TrieNodeException(e);
		}
	}

	public void writeValue(File file, Object value) throws RW_TrieNodeException {
		try {
			mapper().writeValue(file, value);
		} catch (TypePreservingMapperException e) {
			throw new RW_TrieNodeException(e);
		}
	}

	public TrieNode readNode(File nodeFile) throws RW_TrieNodeException {
		TrieNode node = null;
		try {
			node = mapper().readValue(nodeFile, TrieNode.class);
		} catch (TypePreservingMapperException e) {
			throw new RW_TrieNodeException(e);
		}
		return node;
	}

	public <T extends Object> T readValue(File file, Class<T> clazz) throws RW_TrieNodeException {
		T value = null;
		try {
			value = mapper().readValue(file, clazz);
		} catch (TypePreservingMapperException e) {
			throw new RW_TrieNodeException(e);
		}
		return value;
	}
}
