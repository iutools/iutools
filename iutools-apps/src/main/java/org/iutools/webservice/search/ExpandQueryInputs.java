package org.iutools.webservice.search;

import org.iutools.webservice.ServiceInputs;

public class ExpandQueryInputs extends ServiceInputs {

	public String origQuery = null;

	public ExpandQueryInputs() {}

	public ExpandQueryInputs(String _query) {
		this.origQuery = _query;
	}
}
