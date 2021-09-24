package org.iutools.webservice.search;

import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class ExpandQueryInputs extends ServiceInputs {

	public String origQuery = null;

	public ExpandQueryInputs() throws ServiceException {
		init__ExpandQueryInputs((String)null);
	}

	public ExpandQueryInputs(String _query) throws ServiceException {
		init__ExpandQueryInputs(_query);
	}

	protected void init__ExpandQueryInputs(String _query) throws ServiceException {
		this.origQuery = _query;
		validate();
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return asMap();
	}
}
