package org.iutools.webservice;

//import org.apache.jmeter.reporters.Summariser;
//import org.apache.jmeter.save.SaveService;
//import org.apache.jmeter.util.JMeterUtils;
//import org.apache.jorphan.collections.HashTree;

import java.io.File;

public class LoadTest {

	public static void main(String[] args) throws Exception {

//		// JMeter Engine
//		StandardJMeterEngine jmeter = new StandardJMeterEngine();
//
//		// Initialize Properties, logging, locale, etc.
//		JMeterUtils.loadJMeterProperties("/tmp/jmeter/bin/jmeter.properties");
//		JMeterUtils.setJMeterHome("/tmp/jmeter");
//		JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
//		JMeterUtils.initLocale();
//
//		// Initialize JMeter SaveService
//		SaveService.loadProperties();
//
//		// Load Test Plan
//		HashTree testPlanTree = SaveService.loadTree(new File("/tmp/jmeter/bin/test.jmx"));
//
//		Summariser summer = null;
//		String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
//		if (summariserName.length() > 0) {
//			summer = new Summariser(summariserName);
//		}
//
//
//		// Store execution results into a .jtl file
//		String logFile = "/tmp/jmeter/bin/test.jtl";
//		ResultCollector logger = new ResultCollector(summer);
//		logger.setFilename(logFile);
//		testPlanTree.add(testPlanTree.getArray()[0], logger);
//
//		// Run JMeter Test
//		jmeter.configure(testPlanTree);
//		jmeter.run();
	}
}

