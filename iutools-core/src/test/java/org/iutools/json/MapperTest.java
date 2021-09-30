package org.iutools.json;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MapperTest {

	////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////

	@Test
	public void test__Mapper__Synopsis() throws Exception {
		// Use this mapper as you would a normal Jackson ObjectMapper.
		// The only difference is that it may serialize/deserialize certain classes
		// differently (in particular, lang3.Pair).
		Mapper mapper = new Mapper();
		Map<String,Object> obj = new HashMap<String,Object>();
		obj.put("string", "hello word");
		obj.put("pair", Pair.of(1,1));

		String origJson = mapper.writeValueAsString(obj);
		obj = mapper.readValue(origJson, obj.getClass());
		String deserializedJson = mapper.writeValueAsString(obj);
	}

	////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////

	@Test
	public void test__Mapper__SerializeThenDeserialize__VariousCases() throws Exception {
		Map<String,Object> someMap = new HashMap<String,Object>();
		{
			someMap.put("string", "hello word");
			someMap.put("pair", Pair.of(1, 1));
		}

		Case[] cases = new Case[] {
			new Case("Map with a pair attribute", someMap)
		};

		Consumer<Case> runner = (aCase) -> {
			try {
				String descr = aCase.descr;
				Object obj = (Object) aCase.data[0];
				Mapper mapper = new Mapper();
				String origJson = mapper.writeValueAsString(obj);
				obj = mapper.readValue(origJson, obj.getClass());
				String deserializedJson = mapper.writeValueAsString(obj);
				AssertString.assertStringEquals(
					descr+"\nDeserialized JSON different from original",
					origJson, deserializedJson
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
			.run();
	}

}
