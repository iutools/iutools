package org.iutools.webservice.log;

import ca.nrc.testing.Asserter;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import org.iutools.webservice.AssertResponse;

public class AssertLogUITaskResponse extends AssertResponse {
	public AssertLogUITaskResponse(LogUITaskResponse _gotObject, String mess) {
		super(_gotObject, mess);
	}

}
