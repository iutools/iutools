package org.iutools.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
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

	@Test
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
		Person homer = new Person("homer simpson");
		processor.insertObject(homer, new Row2Person());

		// If the table already contains the object, then by default, insertObject
		// will replace the existing row by a row that corresponds to the provided
		// object.
		//
		// However, if you feed replace=false, then the method will raise an exception.
		boolean replace = false;
		try {
			processor.insertObject(homer, new Row2Person(), replace);
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
	public static class Person  {
		public String name = null;
		public String gender = null;
		public int age = -1;

		// Empty constructor for Jackson serialization
		public Person() {
			return;
		}

		public Person(String _name) {
			this.name = _name;
			return;
		}
	}

	// Defines the schema of the SQL table used to store the Person objects
	public static class PersonSchema extends TableSchema {
		public PersonSchema() {
			super("Person", "name");
		}

		@Override
		public String[] unsortedColumnNames() {
			return new String[] {"name", "gender", "age"};
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

	public static class Row2Person extends Row2Pojo<Person> {

		protected ObjectMapper mapper = new ObjectMapper();

		public Row2Person() {
			super(new PersonSchema(), new Person());
		}

		@Override
		public void convertPojoAttributes(Person pojo, JSONObject rawRow) throws SQLException {
			// Nothing to convert
		}

		@Override
		public Person toPOJO(JSONObject row) throws SQLException {
			String jsonStr = row.toString();
			Person person = null;
			try {
				person = mapper.readValue(jsonStr, Person.class);
			} catch (JsonProcessingException e) {
				throw new SQLException("Error converting SQL row to Person instance", e);
			}

			return person;

		}
	}
}
