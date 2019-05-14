package ca.pirurvik.iutools.webservice;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngine.Type;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.QueryExpanderException;
import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;


public class HelloWorld extends HttpServlet {
	public HelloWorld() {
	};
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
				
		String json = "{\"message\":\"ᓄᓇᕗ\"}";
		writer.println(json);
		writer.close();
	}
}
