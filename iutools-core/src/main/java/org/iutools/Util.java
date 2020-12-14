package org.iutools;

import java.net.URL;

public class Util {

	public static String locateFile(String filename) {
		ClassLoader classLoader = Util.class.getClassLoader();
		Package pk = Util.class.getPackage();
		String packagePath = pk.getName().replace('.', '/');
		String fullFilename = packagePath + "/../" + filename;
		URL res = classLoader.getResource(fullFilename);
		String filePath = res.getPath();
		String filePathRep = filePath.replaceAll("%20", " ");
		return filePathRep;
	}
}
