package org.iutools.script;

import ca.nrc.datastructure.Cloner;
import org.iutools.worddict.MultilingualDictException;

import java.util.Collection;
import java.util.List;
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

	public static void transcodeList(
		TransCoder.Script script, List<String> coll) throws MultilingualDictException {
		for (int ii=0; ii < coll.size(); ii++) {
			String transcoded = null;
			try {
				transcoded = TransCoder.ensureScript(script, coll.get(ii));
			} catch (TransCoderException e) {
				throw new MultilingualDictException(e);
			}
			coll.set(ii, transcoded);
		}
	}

	public static void transcodeArray(
		TransCoder.Script script, String[] arr) throws MultilingualDictException {
		for (int ii=0; ii < arr.length; ii++) {
			String transcoded = null;
			try {
				transcoded = TransCoder.ensureScript(script, arr[ii]);
			} catch (TransCoderException e) {
				throw new MultilingualDictException(e);
			}
			arr[ii] = transcoded;
		}
	}
}