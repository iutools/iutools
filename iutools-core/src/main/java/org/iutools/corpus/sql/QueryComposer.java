package org.iutools.corpus.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.datastructure.SortOrder;
import org.iutools.datastructure.SortOrderException;

import java.sql.SQLException;

import static ca.nrc.dtrc.elasticsearch.request.Sort.Order;

public class QueryComposer {

	public static String sqlOrderBy(String... sortCriteria) throws SQLException {
		String sql = "";
		int critCounter = 0;
		for (String criterion: sortCriteria) {
			critCounter++;
			Pair<String,Order> colNameAndOrder = null;
			try {
				colNameAndOrder = SortOrder.parseSortOrderDescr(criterion);
			} catch (SortOrderException e) {
				throw new SQLException(e);
			}
			if (critCounter == 1) {
				sql += "ORDER BY ";
			} else {
				sql += ", ";
			}
			sql += "`"+colNameAndOrder.getLeft()+"` "+colNameAndOrder.getRight().toString().toUpperCase();
		}
		return sql;
	}


}
