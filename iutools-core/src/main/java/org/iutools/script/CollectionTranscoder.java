package org.iutools.script;

import ca.nrc.datastructure.Cloner;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Use this class to transcode collections */
public class CollectionTranscoder {

	public static  <V extends Object> void transcodeKeys(
		TransCoder.Script script, Map<String, V> origMap) throws TransCoderException {
		Set<String> keys = null;
		try {
			keys = Cloner.clone(origMap.keySet());
		} catch (Cloner.ClonerException e) {
			throw new TransCoderException(e);
		}
		for (String aKey: keys) {
			String newKey = TransCoder.ensureScript(script, aKey);
			if (!newKey.equals(aKey)) {
				if (origMap.containsKey(newKey)) {
					throw new TransCoderException(
					"Cannot transcode key " + aKey + " to " + newKey +
					" because the map already has an entry with the transcoded key.\n" +
					"Value associated with transcoded key is: " + origMap.get(newKey));
				}
				origMap.put(newKey, origMap.get(aKey));
				origMap.remove(aKey);
			}
		}
	}

	public static  void transcodeStrValues(
		TransCoder.Script script, Map<String, String> mapToTranscode) throws TransCoderException {
		for (String key: mapToTranscode.keySet()) {
			String val = mapToTranscode.get(key);
			mapToTranscode.put(key, TransCoder.ensureScript(script, TransCoder.ensureScript(script, val)));
		}
	}


	public static <V extends Collection<String>> void transcodeCollValues(
		TransCoder.Script script, Map<String,V> mapToTranscode) throws TransCoderException {

		for (String key: mapToTranscode.keySet()) {
			Collection<String> origValue = mapToTranscode.get(key);
			Collection<String> transcValue = null;
			try {
				transcValue = Cloner.clone(origValue);
			} catch (Cloner.ClonerException e) {
				throw new TransCoderException(e);
			}
			transcValue.clear();
			for (String elt: origValue) {
				transcValue.add(TransCoder.ensureScript(script, elt));
			}
			mapToTranscode.put(key, (V)transcValue);
		}
	}
}
