package org.iutools.script;

import ca.nrc.datastructure.Cloner;
import org.iutools.worddict.MachineGeneratedDictException;

import java.util.*;

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

	public static  <K extends Object> void transcodeValues(
		TransCoder.Script script, Map<K, String> origMap) throws TransCoderException {
		Set<String> keys = null;
		for (K aKey: origMap.keySet()) {
			String oldValue = origMap.get(aKey);
			String newValue = TransCoder.ensureScript(script, oldValue);
			origMap.put(aKey, newValue);
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
		TransCoder.Script script, List<String> coll) throws MachineGeneratedDictException {
		for (int ii=0; ii < coll.size(); ii++) {
			String transcoded = null;
			try {
				transcoded = TransCoder.ensureScript(script, coll.get(ii));
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
			coll.set(ii, transcoded);
		}
	}

	public static Set<String> transcodeSet(
		TransCoder.Script script, Set<String> origSet) throws MachineGeneratedDictException {
		Set<String> transcodedSet = new HashSet<String>();
		for (String elt: origSet) {
			String transcoded = null;
			try {
				transcoded = TransCoder.ensureScript(script, elt);
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
			transcodedSet.add(transcoded);
		}
		return transcodedSet;
	}

	public static void transcodeArray(
		TransCoder.Script script, String[] arr) throws MachineGeneratedDictException {
		for (int ii=0; ii < arr.length; ii++) {
			String transcoded = null;
			try {
				transcoded = TransCoder.ensureScript(script, arr[ii]);
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
			arr[ii] = transcoded;
		}
	}
}
