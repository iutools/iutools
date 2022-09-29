package org.iutools.corpus;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;


public class CorpusSqlHandler {

    static String corpusDataTableName = "CorpusData";
    static String corpusDataDecompositionTableName = "CorpusDataDecomposition";

    String userName;
    String password;
    String dbms = "drizzle";
    String serverName = "localhost";
    String portNumber = "3306";
    String dbName;
    Connection connection;

    String corpusName;


    public CorpusSqlHandler(String _userName, String _password, String _dbName) {
        this.userName = _userName;
        this.password = _password;
        this.dbName = _dbName;
    }

    public Connection openConnection() throws CorpusSqlHandlerException {
		  try {
			  Class.forName("org.drizzle.jdbc.DrizzleDriver");
		  } catch (ClassNotFoundException e) {
		  		throw new CorpusSqlHandlerException(
		  			"Could not load DrizzleDriver", e);
		  }

		  Connection conn = null;
		  try {
			  Properties connectionProps = new Properties();
			  connectionProps.put("user", this.userName);
			  connectionProps.put("password", this.password);

			  if (this.dbms.equals("mysql")) {
				  conn = DriverManager.getConnection(
				  "jdbc:" + this.dbms + "://" +
				  this.serverName +
				  ":" + this.portNumber +
				  "/" +
				  this.dbName
				  ,
				  this.userName, this.password
				  );
			  } else if (this.dbms.equals("derby")) {
				  conn = DriverManager.getConnection(
				  "jdbc:" + this.dbms + ":" +
				  this.dbName +
				  ";create=true",
				  connectionProps);
			  } else if (this.dbms.equals("drizzle")) {
				  conn = DriverManager.getConnection(
				  "jdbc:" + this.dbms + "://" +
				  this.userName + "@" + this.serverName +
				  ":" + this.portNumber +
				  "/" +
				  this.dbName,
				  connectionProps);
			  }
		  } catch (SQLException e) {
		  		String mess =
					"Could not connect to database.\n  dbMane="+dbName+
					"\n  userName="+userName+"\n  portNumber="+portNumber;
		  		throw new CorpusSqlHandlerException(mess, e);
		  }
			  this.connection = conn;
			  return conn;
    }

    public void closeConnection() throws SQLException {
        this.connection.close();
    }

    public ResultSet query(String query) {
        ResultSet rs = null;
        try {
            Statement stmt = this.connection.createStatement();
            rs = stmt.executeQuery(query);
            if (!rs.next()) {
                rs = null;
            }
        } catch (SQLException throwables) {
        }
        return rs;
    }

public String[][] getDecompositionsForWord(String word) {
    String[][] decompositions = new String[0][];
    String query = "SELECT decompositions_sample " +
            "FROM CorpusData " +
            "WHERE "+
            "word='"+word+"'" +
            ";";
    Gson gson = new Gson();
    try {
        ResultSet rs = this.query(query);
        if ( rs != null ) {
            String decompositionStr = rs.getString("decompositions_sample");
            decompositions = gson.fromJson(rs.getString("decompositions_sample"), (Type) String[][].class);
        }
    } catch (SQLException throwables) {
    }
    return decompositions;
}

    public WordInfo getInfoForWord(String word) {
        WordInfo wordInfo = null;
        String queryStr = "SELECT * FROM CorpusData WHERE word='" + word + "'";
        ResultSet rs = query(queryStr);
        wordInfo = __makeWordInfo(rs);
        return wordInfo;
    }

    public WordInfo getInfoForWord(int noid) {
        WordInfo wordInfo = null;
        String queryStr = "SELECT * FROM CorpusData WHERE noid='" + noid + "'";
        ResultSet rs = query(queryStr);
        wordInfo = __makeWordInfo(rs);
        return wordInfo;
    }

    public WordInfo __makeWordInfo(ResultSet rs) {
        WordInfo wordInfo = null;
        Gson gson = new Gson();
        try {
            if (rs != null) {
                int recordId = Integer.parseInt(rs.getString("noid"));
                String word = rs.getString("word");
//                String[][] decompositions = getDecompositionsForWord(word);
                String[][] decompositions = gson.fromJson(rs.getString("decompositions_sample"), (Type) String[][].class);
                int nbDecompositions = decompositions.length;
                wordInfo = new WordInfo(word);
                wordInfo.setDecompositions(decompositions,nbDecompositions);
                wordInfo.setFrequency(Long.parseLong(rs.getString("frequency")));

            }
        } catch (SQLException | CompiledCorpusException throwables) {
        }
        return wordInfo;
    }

    public WordIterator<WordInfo> getWordIterator() {
        Iterator<WordInfo> wordIterator = new WordIterator<WordInfo>();
        return (WordIterator<WordInfo>) wordIterator;
    }

    public String[] getWordsWithNoDecomposition() throws SQLException {
        String[] wordsWithNoDecomposition;
        List<String> wordsWithNoDecompositionAL = new ArrayList<String>();
//        String queryStr = "SELECT word FROM CorpusData WHERE " +
//                "  NOT EXISTS (SELECT * FROM CorpusDataDecomposition WHERE " +
//                "                CorpusDataDecomposition.CorpusData_noid=CorpusData.noid)";
        String queryStr = "SELECT word FROM CorpusData " +
                "WHERE `decompositions_sample`='[]'";
        ResultSet rs = query(queryStr);
        if (rs != null) {
            String word = rs.getString("word");
            wordsWithNoDecompositionAL.add(word);
            while (rs.next()) {
                word = rs.getString("word");
                wordsWithNoDecompositionAL.add(word);
            }
        }
        return wordsWithNoDecompositionAL.toArray(new String[0]);
    }

    public String[] getWordsWithDecompositions() throws SQLException {
        String[] wordsWithNoDecomposition;
        List<String> wordsWithNoDecompositionAL = new ArrayList<String>();
//        String queryStr = "SELECT word FROM CorpusData WHERE " +
//                "  EXISTS (SELECT * FROM CorpusDataDecomposition WHERE " +
//                "                CorpusDataDecomposition.CorpusData_noid=CorpusData.noid)";
        String queryStr = "SELECT word FROM CorpusData " +
                "WHERE `decompositions_sample`<>'[]'";
        ResultSet rs = query(queryStr);
        if (rs != null) {
            String word = rs.getString("word");
            wordsWithNoDecompositionAL.add(word);
            while (rs.next()) {
                word = rs.getString("word");
                wordsWithNoDecompositionAL.add(word);
            }
        }
        return wordsWithNoDecompositionAL.toArray(new String[0]);
    }

    public int getNbWordsWithNgram(String ngram) {
        return getNbWordsWithNgram(ngram,false);
    }
    public int getNbWordsWithNgram(String ngram, boolean onlyWordsWithDecompositions) {
        int nb = 0;
        String queryStr = "SELECT count(word) FROM CorpusData WHERE word LIKE '%"+ngram+"%'";
        if (onlyWordsWithDecompositions) {
//            queryStr += "AND" +
//                    " EXISTS (SELECT * FROM CorpusDataDecomposition WHERE " +
//                    "            CorpusDataDecomposition.CorpusData_noid=CorpusData.noid)";
            queryStr += "AND `decompositions_sample`<>'[]'";
        }
        queryStr += ";";
        ResultSet rs = query(queryStr);
        if (rs != null) {
            try {
                nb = Integer.parseInt(rs.getString("count(word)"));
            } catch (SQLException throwables) {
            }
        }
        return nb;
    }

    /*
     * Assume that the sequence of morphemes has the form like {gaq/1vn} {aq/2nv}
     */
    public int getNbWordsWithSequenceOfMorphemes(String sequenceOfMorphemes) {
        String adjustedSequenceOfMorphemes = sequenceOfMorphemes.replaceAll(" ",",");
        adjustedSequenceOfMorphemes = adjustedSequenceOfMorphemes.replaceAll("\\{","\"");
        adjustedSequenceOfMorphemes = adjustedSequenceOfMorphemes.replaceAll("\\}","\"");
        int nb = 0;
        String queryStr = "SELECT count(word) FROM CorpusData WHERE decompositions_sample LIKE '%"+adjustedSequenceOfMorphemes+"%'";
        queryStr += ";";
        ResultSet rs = query(queryStr);
        if (rs != null) {
            try {
                nb = Integer.parseInt(rs.getString("count(word)"));
            } catch (SQLException throwables) {
            }
        }
        return nb;
    }

    public String[] getWordsWithNgram(String ngram) throws SQLException {
        return getWordsWithNgram(ngram,false);
    }
    public String[] getWordsWithNgram(String ngram, boolean onlyWordsWithDecompositions) throws SQLException {
        String[] wordsWithNgram;
        List<String> wordsWithNoDecompositionAL = new ArrayList<String>();
        String queryStr = "SELECT word FROM CorpusData WHERE word LIKE '%"+ngram+"%'";
        if (onlyWordsWithDecompositions) {
//            queryStr += "AND" +
//                    " EXISTS (SELECT * FROM CorpusDataDecomposition WHERE " +
//                    "            CorpusDataDecomposition.CorpusData_noid=CorpusData.noid)";
            queryStr += "AND `decompositions_sample`<>'[]'";
        }
        queryStr += ";";
        ResultSet rs = query(queryStr);
        if (rs != null) {
            String word = rs.getString("word");
            wordsWithNoDecompositionAL.add(word);
            while (rs.next()) {
                word = rs.getString("word");
                wordsWithNoDecompositionAL.add(word);
            }
        }
        return wordsWithNoDecompositionAL.toArray(new String[0]);
    }


    //------------------------------------------------------------------------------

    public class WordIterator<WordInfo> implements Iterator {

        int index = 0;

        @Override
        public boolean hasNext() {
            return true;
        }

        /*
         * next() returns the word info for the next record in the database.
         * When the end of the table has been reached, it returns NULL;
         * so hasNext() is of no use and is made to always return TRUE.
         */
        @Override
        public WordInfo next() {
            WordInfo wordInfo = null;
            String queryStr = "SELECT * FROM CorpusData WHERE noid > "+index+" ORDER BY noid LIMIT 1 ";
            ResultSet rs = query(queryStr);
            try {
                if (rs != null) {
                    wordInfo = (WordInfo) __makeWordInfo(rs);
                    index = Integer.parseInt(rs.getString("noid"));
//                    rs.previous();
                }
            } catch (SQLException throwables) {
                return null;
            }
            return wordInfo;
        }

        public int getIndex() {
            return index;
        }

    }


}