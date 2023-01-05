package org.iutools.webservice.worddict;

import static ca.nrc.testing.RunOnCases.Case;

public class EndpointCase extends Case {
	public String expectError = null;
	public EndpointCase(String _descr, Object... _data) {
		super(_descr, _data);
	}

	public EndpointCase raisesError(String _error) {
		this.expectError = _error;
		return this;
	}

}
