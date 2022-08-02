package org.iutools.corpus.sql;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;

import java.io.*;
import java.nio.file.Path;

/**
 * Used to convert the JSON file for a corpus to a SQL format
 */
public class Json2SQL {

	public void convert(Path jsonFilePath, Path sqlFilePath, String corpusName) throws CompiledCorpusException {
		File sqlFile = sqlFilePath.toFile();
		if (sqlFile.exists()) {
			sqlFile.delete();
		}
		PrintWriter sqlFileWriter
				 = null;
		try {
			sqlFileWriter = new PrintWriter(new BufferedWriter(new FileWriter(sqlFile)));
		} catch (IOException e) {
			throw new CompiledCorpusException(
				"Problem opening corpus sql file for writing: "+sqlFile,
				e);
		}


		sqlFileWriter.println(generateSqlIntroForCorpusData());
		sqlFileWriter.println(generateSqlInsertStatementOpenForCorpusData());

		int idRecordInCorpusDataTable = 1;
		int idRecordInCorpusDataDecompositionTable = 0;
		try {
			ObjectStreamReader reader = new ObjectStreamReader(jsonFilePath.toFile());
			while (true) {
				WordInfo wordInfo = null;
				wordInfo = (WordInfo) reader.readObject();
				if (wordInfo == null) break;
				if (idRecordInCorpusDataTable != 1) {
					 sqlFileWriter.println(",");
				}
				String insertLine = generateSqlInsertStatementValuesForCorpusDataRecord(wordInfo,corpusName,idRecordInCorpusDataTable);
				sqlFileWriter.print(insertLine);
				idRecordInCorpusDataTable++;
			}
		} catch (IOException | ClassNotFoundException| ObjectStreamReaderException e) {
			throw new CompiledCorpusException(
				"Problem reading word info from json file: "+jsonFilePath, e);
		}

		sqlFileWriter.println(generateSqlInsertClose());
		sqlFileWriter.close();
	}


	public String generateSqlIntroForCorpusData() {
		String wordsTable = new WordInfoSchema().tableName;
	  String intro =
				 "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
							"SET time_zone = \"+00:00\";\n" +
							"\n" +
							"--\n" +
							"-- Database: `???`\n" +
							"--\n" +
							"\n" +
							"-- --------------------------------------------------------\n" +
							"\n" +
							"--\n" +
							"-- Table structure for table `"+wordsTable+"`\n" +
							"--\n" +
							"\n" +
							"CREATE TABLE IF NOT EXISTS `"+wordsTable+"` (\n" +
							"  `word` text NOT NULL,\n" +
							"  `corpus_name` text NOT NULL,\n" +
							"  `_detect_language` tinyint(1) NOT NULL DEFAULT '1',\n" +
							"  `content` text DEFAULT NULL,\n" +
							"  `creationDate` date DEFAULT NULL,\n" +
							"  `frequency` int(11) NOT NULL,\n" +
							"  `lang` varchar(2) NOT NULL,\n" +
							"  `shortDescription` text DEFAULT NULL,\n" +
							"  `decompositions_sample` text DEFAULT NULL,\n" +
							"   PRIMARY KEY (word)\n" +
							") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
							"\n" +
							"\n" +
							"--\n" +
							"-- Dumping data for table `"+wordsTable+"`\n" +
							"--\n" +
							"\n";
	  return intro;
	}

	public String generateSqlInsertStatementOpenForCorpusData() {
	  String statement = "INSERT INTO `CorpusData` (" +
				 "`word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`" +
				 ") VALUES";
	  return statement;
	}

	public String generateSqlInsertStatementValuesForCorpusDataRecord(WordInfo wordInfo, String corpusName, int idWordInTable) throws CompiledCorpusException {
		try {
			String decompositions =
				new ObjectMapper().writeValueAsString(wordInfo.decompositionsSample);
				String statement = "('"+wordInfo.word+"', '"+corpusName+"', "+wordInfo.frequency+", '"+wordInfo.lang+"', '"+decompositions+"')";
				return statement;
		} catch (JsonProcessingException e) {
			throw new CompiledCorpusException(
			"Problem serializing decompositions sample for word " + wordInfo.word,
			e
			);
		}
	}
//
//	public String generateSqlParametersForCorpusData(int idRecordInCorpusDataTable) {
//	  String parametersStmts = "\n" +
//				 "--\n" +
//				 "-- Indexes for dumped tables\n" +
//				 "--\n" +
//				 "\n" +
//				 "--\n" +
//				 "-- Indexes for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
//				 "--\n" +
//				 "ALTER TABLE `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
//				 "  ADD PRIMARY KEY (`noid`);\n" +
//				 "\n" +
//				 "--\n" +
//				 "-- AUTO_INCREMENT for dumped tables\n" +
//				 "--\n" +
//				 "\n" +
//				 "--\n" +
//				 "-- AUTO_INCREMENT for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
//				 "--\n" +
//				 "ALTER TABLE `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
//				 "  MODIFY `noid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT="+
//				 idRecordInCorpusDataTable+
//				 ";\n";
//	  return parametersStmts;
//	}
//
//	// Table CorpusDataDecomposition
//
//	public String generateSqlIntroForCorpusDataDecomposition() {
//	  String intro =
//				 "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
//							"SET time_zone = \"+00:00\";\n" +
//							"\n" +
//							"--\n" +
//							"-- Database: `???`\n" +
//							"--\n" +
//							"\n" +
//							"-- --------------------------------------------------------\n" +
//							"\n" +
//							"--\n" +
//							"-- Table structure for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
//							"--\n" +
//							"\n" +
//							"CREATE TABLE IF NOT EXISTS `"+CorpusSqlHandler.corpusDataDecompositionTableName+"` (\n" +
//	//                        "  `id` int(11) NOT NULL,\n" +
//							"  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
//							"  `CorpusData_noid` int(11) NOT NULL,\n" +
//	//                        "  `decompositionStr` text DEFAULT NULL\n" +
//							"  `decompositionStr` text DEFAULT NULL,\n" +
//							"   PRIMARY KEY (id)\n" +
//							") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
//							"\n" +
//							"\n";
//	  return intro;
//	}
//
//	public String generateSqlInsertStatementOpenForCorpusDataDecomposition() {
//	  String comment =
//				 "--\n" +
//							"-- Dumping data for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
//							"--\n";
//	  String statement = "INSERT INTO `"+CorpusSqlHandler.corpusDataDecompositionTableName+"` (" +
//				 "`CorpusData_noid`, `decompositionStr`" +
//				 ") VALUES";
//	  return comment+statement;
//	}
//	public String generateSqlInsertStatementValuesForCorpusDataDecompositionRecord(WordInfo wordInfo, int idWordInTable) {
//	  String decompsStatement = null;
//	  String[][] wordDecompositions = wordInfo.decompositionsSample;
//	  if (wordDecompositions.length != 0) {
//			List<String> decompsAL = new ArrayList<String>();
//			for (String[] wordDecomposition : wordDecompositions) {
//				 String statement = "(" + idWordInTable + ", '" + String.join(",",wordDecomposition) + "')";
//				 decompsAL.add(statement);
//			}
//			String[] decompsStatements = decompsAL.toArray(new String[0]);
//			decompsStatement = String.join(",\n", decompsStatements);
//	  }
//	  return decompsStatement;
//	}
//
//	public String generateSqlParametersForCorpusDataDecomposition() {
//	  String parametersStmts = "\n" +
//				 "--\n" +
//				 "-- Indexes for dumped tables\n" +
//				 "--\n" +
//				 "\n" +
//				 "--\n" +
//				 "-- Indexes for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
//				 "--\n" +
//				 "ALTER TABLE `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
//				 "  ADD PRIMARY KEY (`id`);\n" +
//				 "\n" +
//				 "--\n" +
//				 "-- AUTO_INCREMENT for dumped tables\n" +
//				 "--\n" +
//				 "\n" +
//				 "--\n" +
//				 "-- AUTO_INCREMENT for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
//				 "--\n" +
//				 "ALTER TABLE `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
//				 "  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1;\n";
//	  return parametersStmts;
//	}
//
//	// Common to both tables
//
	public String generateSqlInsertClose() {
	  return ";";
	}
//
//	//    public int getNextAutoIncrementIndexInCorpusData() throws SQLException, ClassNotFoundException {
//	//        int nextIndexInTable = 0;
//	//        String userName = "benoitfa_icca";
//	//        String password = "1ijiqaqtunga!";
//	//        String dbName = "benoitfa_iudata";
//	//        CorpusSqlHandler corpusSqlHandler = new CorpusSqlHandler(userName,password,dbName);
//	//        Connection con = corpusSqlHandler.openConnection();
//	//        return nextIndexInTable;
//	//    }
//
//
//	// Writing the SQL files
//
//	public void writeCorpusDataSqlFiles(String corpusJsonFilePathname,String corpusSqlFilePathname,String corpusName) throws IOException, ObjectStreamReaderException, ClassNotFoundException {
//	  File corpusDataSqlFile = new File(corpusSqlFilePathname);
//	  if (corpusDataSqlFile.exists()) {
//			corpusDataSqlFile.delete();
//	  }
//	  PrintWriter corpusDataSqlWriter
//				 = new PrintWriter(new BufferedWriter(new FileWriter(corpusDataSqlFile)));
//
//
//	  corpusDataSqlWriter.println(generateSqlIntroForCorpusData());
//	  corpusDataSqlWriter.println(generateSqlInsertStatementOpenForCorpusData());
//
//	  int idRecordInCorpusDataTable = 1;
//	  int idRecordInCorpusDataDecompositionTable = 0;
//	  ObjectStreamReader reader = new ObjectStreamReader(new File(corpusJsonFilePathname));
//	  while (true) {
//			WordInfo wordInfo = (WordInfo) reader.readObject();
//			if (wordInfo == null) break;
//			if (idRecordInCorpusDataTable != 1) {
//				 corpusDataSqlWriter.println(",");
//			}
//			String insertLine = generateSqlInsertStatementValuesForCorpusDataRecord(wordInfo,corpusName,idRecordInCorpusDataTable);
//			corpusDataSqlWriter.print(insertLine);
//			idRecordInCorpusDataTable++;
//	  }
//
//	  corpusDataSqlWriter.println(generateSqlInsertClose());
//	  corpusDataSqlWriter.close();
//	}
//


}
