package org.iutools.corpus;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import com.google.gson.Gson;

import java.io.*;


public class CorpusSqlWriter {

    public CorpusSqlWriter() throws IOException {
    }

    // Table CorpusData

    public String generateSqlIntroStmtForCorpusData() {
        String intro =
                "SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
                        "SET time_zone = \"+00:00\";\n" +
                        "\n" +
                        "--\n" +
                        "-- Database: `???`\n" +
                        "--\n" +
                        "\n" +
                        "-- --------------------------------------------------------\n" +
                        "\n";
        return intro;
    }
    public String generateTableCreationSqlStmtForCorpusData() throws IOException {
        String tableCreationSqlStmt =
                "--\n" +
                        "-- Table structure for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                        "--\n" +
                        "\n" +
                        "CREATE TABLE IF NOT EXISTS `"+CorpusSqlHandler.corpusDataTableName+"` (\n" +
//                        "  `noid` int(11) NOT NULL,\n" +
                        "  `noid` int(11) NOT NULL AUTO_INCREMENT,\n" +
                        "  `word` text NOT NULL,\n" +
                        "  `corpus_name` text  NOT NULL,\n" +
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
                        "\n";
        return tableCreationSqlStmt;
    }

    public String generateSqlInsertStatementOpenForCorpusData() {
        String statement =
                "--\n" +
                "-- Dumping data for table `"+CorpusSqlHandler.corpusDataTableName+"`\n" +
                "--\n" +
                "\n" +
        "INSERT INTO `CorpusData` (" +
//                "`noid`, `word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`" +
                "`word`, `corpus_name`, `frequency`, `lang`, `decompositions_sample`" +
                ") VALUES";
        return statement;
    }
    public String generateSqlInsertStatementValuesForCorpusDataRecord(WordInfo wordInfo, String corpusName, int idWordInTable) {
        Gson gson = new Gson();
        String decompositions = gson.toJson(wordInfo.decompositionsSample);
        String statement = "('"+wordInfo.word+"', '"+corpusName+"', "+wordInfo.frequency+", '"+wordInfo.lang+"', '"+decompositions+"')";
        return statement;
    }


    public String generateSqlInsertClose() {
        return ";";
    }


    // Writing the SQL files
public void writeTableCreationSqlFileForCorpusData(String corpusSqlFilePathname) throws IOException {
    File corpusDataSqlFile = new File(corpusSqlFilePathname);
    PrintWriter sqlWriter
            = new PrintWriter(new BufferedWriter(new FileWriter(corpusDataSqlFile)));
    String introComments = generateSqlIntroStmtForCorpusData();
    String tableCreationStmt = generateTableCreationSqlStmtForCorpusData();
    sqlWriter.println(introComments);
    sqlWriter.println(tableCreationStmt);
    sqlWriter.close();
}
    public void writeDataDumpSqlFileForCorpusData(String corpusJsonFilePathname,String corpusSqlFilePathname,String corpusName) throws IOException, ObjectStreamReaderException, ClassNotFoundException {
        File corpusDataSqlFile = new File(corpusSqlFilePathname);
        PrintWriter corpusDataSqlWriter
                = new PrintWriter(new BufferedWriter(new FileWriter(corpusDataSqlFile)));


        corpusDataSqlWriter.println(generateSqlIntroStmtForCorpusData());
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
        CorpusSqlWriter writer = new CorpusSqlWriter();
        if (args.length==0) {
            usage("No input arguments.");
            System.exit(1);
        } else if (args[0].equals("--create-table")) {
            if (args.length<2) {
                usage("Missing argument: filepath for sql output file."+
                        "\narguments given: "+displayArguments(args));
                System.exit(1);
            } else if (args.length>2) {
                usage("Too many arguments."+
                        "\narguments given: "+String.join(" ",args));
                System.exit(1);
            } else {
                String sqlFilePathname = args[1];
                writer.writeTableCreationSqlFileForCorpusData(sqlFilePathname);
            }
        } else if (args[0].equals("--dump-data")) {
            if (args.length<4) {
                usage("Missing arguments."+
                        "\narguments given: "+displayArguments(args));
                System.exit(1);
            } else if (args.length>4) {
                usage("Too many arguments."+
                        "\narguments given: "+displayArguments(args));
                System.exit(1);
            } else {
                String corpusJsonFilePathname = args[1];
                String corpusSqlFilePathname = args[2];
                String corpusName = args[3];
                writer.writeDataDumpSqlFileForCorpusData(corpusJsonFilePathname,corpusSqlFilePathname,corpusName);
            }
        } else {
            usage("Wrong argument(s)."+
                    "\narguments given:\n"+displayArguments(args));
            System.exit(1);
        }

//        CorpusSqlWriter writer = new CorpusSqlWriter();
//        writer.writeCorpusDataSqlFiles(corpusJsonFilePathname,corpusSqlFilePathname,corpusName);
//        System.out.println("Done.");
    }

    public static void usage(String mess) {
        System.out.println("!!!"+mess+"\n");
        usage();
    }
    public static void usage() {
        String us = "usage:\n  --create-table <sql filepath for table creation>\n"+
                "  --dump-data <corpus json filepath> <sql filepath for data insertions> <corpus name>\n";
        System.out.println(us);
    }

    public static String displayArguments(String [] args) {
        String argsStr = "";
        for (int i=0; i<args.length; i++) {
            argsStr +=  (i+1)+". "+args[i]+"\n";
        }
        return argsStr;
    }

}
