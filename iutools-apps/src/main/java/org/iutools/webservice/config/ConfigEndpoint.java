package org.iutools.webservice.config;

import ca.nrc.config.ConfigException;
import org.iutools.config.IUConfig;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

/**
 * End point for obtaining configuration properties from the server.
 */
public class ConfigEndpoint extends Endpoint<ConfigInputs, ConfigResult> {
	@Override
	protected ConfigInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, ConfigInputs.class);
	}

	@Override
	public EndpointResult execute(ConfigInputs inputs) throws ServiceException {
		ConfigResult result = new ConfigResult();
		IUConfig config = new IUConfig();
		for (String propName: inputs.propertyNames) {
			Object propValue = null;
			if (propName.equals("org.iutools.apps.feedkback_emails")) {
				try {
					propValue = config.userFeedbackEmails();
				} catch (ConfigException e) {
					throw new ServiceException("Unable to get property "+propName, e);
				}
			}
			result.setProperty(propName, propValue);
		}
		return result;
	}
}
