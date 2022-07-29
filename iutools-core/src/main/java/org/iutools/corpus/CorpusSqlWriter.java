package org.iutools.corpus;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.file.ResourceGetter;
import com.google.gson.Gson;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CorpusSqlWriter {

    public CorpusSqlWriter() throws IOException {
    }

    // -----------------------------------------------------------------------------------------
    // ------------------- Generation of SQL for dumping JSON corpus into DB -------------------
    // -----------------------------------------------------------------------------------------

    // Table CorpusData

    public String generateSqlIntroForCorpusData() {
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
                        "-- Table structure for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                        "--\n" +
                        "\n" +
                        "CREATE TABLE IF NOT EXISTS `"+CorpusSqlHandler.corpusDataTableName+"` (\n" +
//                        "  `noid` int(11) NOT NULL,\n" +
                        "  `noid` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `word` text NOT NULL,\n" +
                        "  `corpus_name` text NOT NULL,\n" +
                        "  `_detect_language` tinyint(1) NOT NULL DEFAULT '1',\n" +
                        "  `content` text DEFAULT NULL,\n" +
                        "  `creationDate` date DEFAULT NULL,\n" +
                        "  `frequency` int(11) NOT NULL,\n" +
                        "  `lang` varchar(2) NOT NULL,\n" +
                        "  `shortDescription` text DEFAULT NULL,\n" +
//                        "  `decompositions_sample` text DEFAULT NULL\n" +
                        "  `decompositions_sample` text DEFAULT NULL,\n" +
                        "   PRIMARY KEY (noid)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                        "\n" +
                        "\n" +
                        "--\n" +
                        "-- Dumping data for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                        "--\n" +
                        "\n";
        return intro;
    }

    public String generateSqlInsertStatementOpenForCorpusData() {
        String statement = "INSERT INTO `CorpusData` (" +
//                "`noid`, `word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`" +
                "`word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`" +
                ") VALUES";
        return statement;
    }
    public String generateSqlInsertStatementValuesForCorpusDataRecord(WordInfo wordInfo, String corpusName, int idWordInTable) {
        Gson gson = new Gson();
        String decompositions = gson.toJson(wordInfo.decompositionsSample);
//        String statement = "("+idWordInTable+", '"+wordInfo.word+"', '"+corpusName+"', "+wordInfo.frequency+", '"+wordInfo.lang+"', '"+decompositions+"')";
        String statement = "('"+wordInfo.word+"', '"+corpusName+"', "+wordInfo.frequency+", '"+wordInfo.lang+"', '"+decompositions+"')";
        return statement;
    }

    public String generateSqlParametersForCorpusData(int idRecordInCorpusDataTable) {
        String parametersStmts = "\n" +
                "--\n" +
                "-- Indexes for dumped tables\n" +
                "--\n" +
                "\n" +
                "--\n" +
                "-- Indexes for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                "--\n" +
                "ALTER TABLE `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                "  ADD PRIMARY KEY (`noid`);\n" +
                "\n" +
                "--\n" +
                "-- AUTO_INCREMENT for dumped tables\n" +
                "--\n" +
                "\n" +
                "--\n" +
                "-- AUTO_INCREMENT for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                "--\n" +
                "ALTER TABLE `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                "  MODIFY `noid` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT="+
                idRecordInCorpusDataTable+
                ";\n";
        return parametersStmts;
    }

    // Table CorpusDataDecomposition

    public String generateSqlIntroForCorpusDataDecomposition() {
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
                        "-- Table structure for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
                        "--\n" +
                        "\n" +
                        "CREATE TABLE IF NOT EXISTS `"+CorpusSqlHandler.corpusDataDecompositionTableName+"` (\n" +
//                        "  `id` int(11) NOT NULL,\n" +
                        "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `CorpusData_noid` int(11) NOT NULL,\n" +
//                        "  `decompositionStr` text DEFAULT NULL\n" +
                        "  `decompositionStr` text DEFAULT NULL,\n" +
                        "   PRIMARY KEY (id)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=latin1;\n" +
                        "\n" +
                        "\n";
        return intro;
    }

    public String generateSqlInsertStatementOpenForCorpusDataDecomposition() {
        String comment =
                "--\n" +
                        "-- Dumping data for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
                        "--\n";
        String statement = "INSERT INTO `"+CorpusSqlHandler.corpusDataDecompositionTableName+"` (" +
                "`CorpusData_noid`, `decompositionStr`" +
                ") VALUES";
        return comment+statement;
    }
    public String generateSqlInsertStatementValuesForCorpusDataDecompositionRecord(WordInfo wordInfo, int idWordInTable) {
        String decompsStatement = null;
        String[][] wordDecompositions = wordInfo.decompositionsSample;
        if (wordDecompositions.length != 0) {
            List<String> decompsAL = new ArrayList<String>();
            for (String[] wordDecomposition : wordDecompositions) {
                String statement = "(" + idWordInTable + ", '" + String.join(",",wordDecomposition) + "')";
                decompsAL.add(statement);
            }
            String[] decompsStatements = decompsAL.toArray(new String[0]);
            decompsStatement = String.join(",\n", decompsStatements);
        }
        return decompsStatement;
    }

    public String generateSqlParametersForCorpusDataDecomposition() {
        String parametersStmts = "\n" +
                "--\n" +
                "-- Indexes for dumped tables\n" +
                "--\n" +
                "\n" +
                "--\n" +
                "-- Indexes for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
                "--\n" +
                "ALTER TABLE `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
                "  ADD PRIMARY KEY (`id`);\n" +
                "\n" +
                "--\n" +
                "-- AUTO_INCREMENT for dumped tables\n" +
                "--\n" +
                "\n" +
                "--\n" +
                "-- AUTO_INCREMENT for table `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
                "--\n" +
                "ALTER TABLE `"+CorpusSqlHandler.corpusDataDecompositionTableName+"`\n" +
                "  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1;\n";
        return parametersStmts;
    }

    // Common to both tables

    public String generateSqlInsertClose() {
        return ";";
    }

//    public int getNextAutoIncrementIndexInCorpusData() throws SQLException, ClassNotFoundException {
//        int nextIndexInTable = 0;
//        String userName = "benoitfa_icca";
//        String password = "1ijiqaqtunga!";
//        String dbName = "benoitfa_iudata";
//        CorpusSqlHandler corpusSqlHandler = new CorpusSqlHandler(userName,password,dbName);
//        Connection con = corpusSqlHandler.openConnection();
//        return nextIndexInTable;
//    }


    // Writing the SQL files

    public void writeCorpusDataSqlFiles(String corpusJsonFilePathname,String corpusSqlFilePathname,String corpusName) throws IOException, ObjectStreamReaderException, ClassNotFoundException {
        File corpusDataSqlFile = new File(corpusSqlFilePathname);
        if (corpusDataSqlFile.exists()) {
            corpusDataSqlFile.delete();
        }
        PrintWriter corpusDataSqlWriter
                = new PrintWriter(new BufferedWriter(new FileWriter(corpusDataSqlFile)));


        corpusDataSqlWriter.println(generateSqlIntroForCorpusData());
        corpusDataSqlWriter.println(generateSqlInsertStatementOpenForCorpusData());

        int idRecordInCorpusDataTable = 1;
        int idRecordInCorpusDataDecompositionTable = 0;
        ObjectStreamReader reader = new ObjectStreamReader(new File(corpusJsonFilePathname));
        while (true) {
            WordInfo wordInfo = (WordInfo) reader.readObject();
            if (wordInfo == null) break;
            if (idRecordInCorpusDataTable != 1) {
                corpusDataSqlWriter.println(",");
            }
            String insertLine = generateSqlInsertStatementValuesForCorpusDataRecord(wordInfo,corpusName,idRecordInCorpusDataTable);
            corpusDataSqlWriter.print(insertLine);
            idRecordInCorpusDataTable++;
        }

        corpusDataSqlWriter.println(generateSqlInsertClose());
        corpusDataSqlWriter.close();
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, ObjectStreamReaderException {
        String corpusJsonFilePathname = args[0];
        String corpusSqlFilePathname = args[1];
        String corpusName = args[2];
        CorpusSqlWriter writer = new CorpusSqlWriter();
        writer.writeCorpusDataSqlFiles(corpusJsonFilePathname,corpusSqlFilePathname,corpusName);
        System.out.println("Done.");
    }

}
