package org.iutools.json;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This mapper uses custom serializers/deserializers for certain classes (for
 * example: lang2.Pair)
 */
public class Mapper extends ObjectMapper {
	public Mapper() {
		super();
		this.registerModule(new SimpleModule().addSerializer(Pair.class, new PairSerializer()));
		this.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
		this.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
	}
}
