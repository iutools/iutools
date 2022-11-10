package org.iutools.datastructure;

import static ca.nrc.dtrc.elasticsearch.request.Sort.Order;
import org.apache.commons.lang3.tuple.Pair;

public class SortOrder {

	public static Pair<String, Order> parseSortOrderDescr(String critStr) throws SortOrderException {
		String field = critStr;
		Order order = Order.asc;
		String[] parts = critStr.split("\\:");
		String errMess = "Invalid sorting criterion string: " + critStr;
		if (parts.length > 2) {
			throw new SortOrderException(
				errMess +
				"\n  Should not have contained more than one occurence of ':'");
		} else if (parts.length == 2) {
			field = parts[0];
			try {
				order = Order.valueOf(parts[1].toLowerCase());
			} catch (Exception e) {
				throw new SortOrderException(
					errMess +
					"\n  invalid sort order '" + parts[1] + "'");
			}
		}
		return Pair.of(field, order);
	}

}
