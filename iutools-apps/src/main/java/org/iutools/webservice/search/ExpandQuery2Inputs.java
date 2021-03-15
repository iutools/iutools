package org.iutools.webservice.search;

import org.iutools.webservice.ServiceInputs;

public class ExpandQuery2Inputs extends ServiceInputs {

	public String origQuery = null;

	public ExpandQuery2Inputs() {}

	public ExpandQuery2Inputs(String _query) {
		this.origQuery = _query;
	}
}
