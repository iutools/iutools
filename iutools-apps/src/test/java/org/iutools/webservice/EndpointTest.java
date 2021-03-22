package org.iutools.webservice;

import ca.nrc.config.ConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.spellchecker.SpellCheckerException;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileNotFoundException;

public abstract class EndpointTest {

	public abstract Endpoint makeEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException, ServiceException;

	private ObjectMapper mapper = new ObjectMapper();

	protected Endpoint endPoint = null;

	@BeforeEach
	public void setUp() throws Exception {
		endPoint = makeEndpoint();
	}
}