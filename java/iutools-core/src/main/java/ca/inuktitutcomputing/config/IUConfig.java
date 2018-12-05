package ca.inuktitutcomputing.config;

import ca.nrc.config.Config;
import ca.nrc.config.ConfigException;

public class IUConfig extends Config {
	
	public static String getIUDataPath() throws ConfigException {
		String iuDataPath = getConfigProperty("ca.nrc.iutools.datapath", true);	
		return iuDataPath;
	}
}
