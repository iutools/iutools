package ca.pirurvik.iutools.webservice;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SearchEndpointTest {

	SearchEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new SearchEndPoint();
		if (!repoBuilt) {
			DedupsterTestHelpers.buildRealisticBugRepo("test-bugs");
		}
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	
	/***********************
	 * VERIFICATION TESTS
	 * @throws Exception 
	 ***********************/	
	
	
	@Test @Ignore
	public void test__TODO1__trainingStatus__returnsNoTraining() {
		Assert.fail("When start training on eclipse data, Java trainingStatus claims no training for that collection");
	}
	
	@Test @Ignore
	public void test__TODO2() {
		Assert.fail("When try train or trainStatus on busy collection, it should say WHEN training was started");
	}
	
	@Test @Ignore
	public void test__TODO3() {
		Assert.fail("Could we move the default model to dedupster-python? That way:\n-We wouldn't have to ship deduster-data (which includes confidential EA data)\n- duplicates_server could identify the path where ITSELF resides and determine locaiton of default model without the need for an env variable\n- NOTE: Only do this if we can tag the model in Git as being binary and as not needing to have every revision tracked.");
	}

	@Test
	public void test__executeEndPoint__DumpsCollectionToWorkspace() throws Exception {
		DedupsterServiceInputs inputs = new DedupsterServiceInputs();
		inputs.collection = "test-bugs";
		inputs.index = "dedupster-test";
		endPoint.executeEndPoint(inputs);
		
		// Sleep a bit to give the 'train' thread time to dump the collection
		// data.
		//
		Thread.sleep(10*1000);
		
		assertDumpedCollectionContainsNLines(inputs.index, inputs.collection, 201);
	}
	


	/***********************
	 * TEST HELPERS
	 ***********************/

	private void assertDumpedCollectionContainsNLines(String index, String collection, int expCount) throws ClassNotFoundException, IOException, DedupsterServiceException, SiameseModelConnectorException {
		String dumpFile = new EndPointHelper().getCollectionWorkspace(index, collection)+"/alldocs.dump.json";		
		long gotCount = FileUtils.readLines(new File(dumpFile)).size();
		
		
		Assert.assertEquals(expCount, gotCount);
		
	}	

}
