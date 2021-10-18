package org.iutools.webservice.transcode;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

public class TranscodeEndpoint extends Endpoint<TranscodeInputs, TranscodeResults> {
	@Override
	protected TranscodeInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return null;
	}

	@Override
	public EndpointResult execute(TranscodeInputs inputs) throws ServiceException {

		TranscodeResults epResults = new TranscodeResults();
		return epResults;
	}
}
