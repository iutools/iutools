package org.iutools.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.corpus.sql.WordInfoSchema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

public class ResourcesTrackerTest {

	@BeforeAll
	public static void beforeAll() throws Exception {
		new QueryProcessor().ensureTableIsDefined(new WordInfoSchema());
	}

	//////////////////////////////////////////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////////////////////////////////////////

	@Test
	public void test__ResourceTracker__Synopsis() throws Exception {
		// ResourceTracker keeps track of all SQL Statement and ResultSets that
		// are created/opened by the following classes:
		//
		// - QueryProcessor
		// - ResultSetWrapper
		// - ResultSetIterator
		// - ResultSetColIterator
		//
		// Whenever one of those classes creates, receives or closes a resource,
		// it should notify the tracker as follows
		//
		Pair<Statement,ResultSet> resources = makeResources();

		Statement stmt = resources.getLeft();
		ResourcesTracker.updateResourceStatus(stmt);

		ResultSet rs = resources.getRight();
		ResourcesTracker.updateResourceStatus(rs);

		// At any point, you can get the number of active resources as follows
		int openedStatements = ResourcesTracker.totalStatements();
		int openedResultSets = ResourcesTracker.totalResultSets();

		// Note that the above only provide an "estimate" of the active resources.
		// To get a perfectly accurate count, you must pass a true argument.
		// Note however that this will be slower than if you just get an estimate.
		openedStatements = ResourcesTracker.totalStatements(true);
		openedResultSets = ResourcesTracker.totalResultSets(true);
	}

	///////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////

	@Test @Disabled
	public void test__NoLeakedResourcesSoFar() throws Exception {
		AssertResourcesTracker.totalStatementsEquals(
			"Total open Statements should have been be 0.\n" +
			"If they weren't, it means some other tests leaked SQL resources", 0);
		AssertResourcesTracker.totalResultSetsEquals(
			"Total open Statements should have been be 0.\n" +
			"If they weren't, it means some other tests leaked SQL resources", 0);
	}


	@Test @Disabled
	public void test__ResourcesTracker__HappyPath() throws Exception {
		int initialRS = ResourcesTracker.totalResultSets();
		int initialStmts = ResourcesTracker.totalStatements();
		System.out.println("Initially, totalResultSets="+initialRS+", totalStatements="+initialStmts);

		// Open some resources and notify the tracker of them
		Pair<Statement, ResultSet> resources = makeResources();
		Statement stmt1 = resources.getLeft();
		ResultSet rs1 = resources.getRight();
		ResourcesTracker.updateResourceStatus(stmt1);
		ResourcesTracker.updateResourceStatus(rs1);

		AssertResourcesTracker.totalStatementsEquals("After opening 1st Statement...", initialStmts+1);
		AssertResourcesTracker.totalResultSetsEquals("After opening 1st ResultSet...", initialRS+1);

		// Open more resources and notify tracker
		resources = makeResources();
		Statement stmt2 = resources.getLeft();
		ResultSet rs2 = resources.getRight();
		ResourcesTracker.updateResourceStatus(stmt2);
		ResourcesTracker.updateResourceStatus(rs2);

		AssertResourcesTracker.totalStatementsEquals("After opening 2nd Statement...", initialStmts+2);
		AssertResourcesTracker.totalResultSetsEquals("After opening 2nd ResultSet...", initialRS+2);

		// Feeding some resource that are already known and still open does not
		// change the counts
		ResourcesTracker.updateResourceStatus(stmt2);
		ResourcesTracker.updateResourceStatus(rs2);

		AssertResourcesTracker.totalStatementsEquals("After re-feeding opened Statement...", initialStmts+2);
		AssertResourcesTracker.totalResultSetsEquals("After re-feeding opened ResultSet...", initialRS+2);

		// Close the 1st pair of resources and update the tracker
		stmt1.close();
		rs1.close();
		ResourcesTracker.updateResourceStatus(stmt1);
		ResourcesTracker.updateResourceStatus(rs1);
		AssertResourcesTracker.totalStatementsEquals("After closing 1st Statement...", initialStmts+1);
		AssertResourcesTracker.totalResultSetsEquals("After closing 1st ResultSet...", initialRS+1);
	}

	///////////////////////////////
	// TEST HELPERS
	///////////////////////////////

	private Pair<Statement,ResultSet> makeResources() throws Exception {
		Pair<Statement, ResultSet> resources = SQLTestHelpers.openManagedResources();
		return resources;
	}
}