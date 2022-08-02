package org.iutools.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class QueryProcessorTest {

	@BeforeEach
	public void setUp() throws Exception {
		QueryProcessor processor = new QueryProcessor();
		PersonSchema personSchema = new PersonSchema();
		processor.dropTable(personSchema.tableName);
		processor.ensureTableIsDefined(new PersonSchema());
//		processor.insertObject(new Person("Homer Simpson"));
//		processor.insertObject(new Person("Marge Simpson"));
	}

	////////////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////////////

	@Test @Disabled
	public void test__QueryProcessorSynopsis() throws Exception {
		// Use a QueryProcessor to process SQL queries.
		QueryProcessor processor = new QueryProcessor();

		// NOTE: Processors come with a pool of connections whose specifics
		// (ex: database name, user name, password, etc...) are defined in the
		// iutools properties file (see ConnectionPool for details).
		//
		// By default, queries are processed using a connection fetched from the poool.
		// Depending on the method used to process the query, the connection may be
		// either closed upon exit or returned by the method (in which case, someone
		// in the call chain is responsible for closing it when done with it).
		//
		
		// You can check if a particular table exists
		boolean exists = processor.tableIsDefined("Person");

		// You can use a processor to insert SQLPersistent object into
		// the database.
		// Note that this will insert the object into the SQL table provided
		// by that object's SQL schema.
		//
		SQLPersistent homer = new Person("homer simpson");
		processor.insertObject(homer);

		// If the table already contains the object, then by default, insertObject
		// will replace the existing row by a row that corresponds to the provided
		// object.
		//
		// However, if you feed replace=false, then the method will raise an exception.
		boolean replace = false;
		try {
			processor.insertObject(homer, replace);
		} catch (SQLException e) {
		}


		// You can execute arbitrary SQL statements as follows
		String sql =
			"DROP TABLE IF EXISTS ?";
	}

	///////////////////////////////////////////////
	// TEST HELPER CLASSES
	///////////////////////////////////////////////

	// A class that can be stored in am SQL table
	public static class Person extends SQLPersistent {
		public String name = null;
		public String gender = null;
		public int age = -1;

		public Person(String _name) {
			super(new PersonSchema());
			this.name = _name;
		}
	}

	// Defines the schema of the SQL table used to store the Person objects
	public static class PersonSchema extends TableSchema {
		public PersonSchema() {
			super("Person", "name");
		}

		@Override
		public String[] schemaStatements() {
			return new String[] {
				"CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n" +
				"  `name` varchar(100) NOT NULL,\n" +
				"  `gender` varchar(1) DEFAULT NULL,\n" +
				"  `age` int(11) DEFAULT -1" +
				");"
			};
		}
	}
}
