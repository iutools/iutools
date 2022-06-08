package org.iutools.concordancer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

import ca.nrc.datastructure.Pair;
import net.loomchild.maligna.ui.console.command.AbstractCommand;
import net.loomchild.maligna.ui.console.command.AlignCommand;
import net.loomchild.maligna.ui.console.command.FormatCommand;
import net.loomchild.maligna.ui.console.command.ModifyCommand;
import net.loomchild.maligna.ui.console.command.ParseCommand;
import org.apache.log4j.Logger;


public class Aligner_Maligna {

	boolean verbose = true;

	int currOrigLine = 0;

	public List<Pair<String,String>> align(List<String> l1Sents, 
		List<String> l2Sents) throws AlignerException {

		String[] l1SentsArr = l1Sents.toArray(new String[l1Sents.size()]);
		String[] l2SentsArr = l2Sents.toArray(new String[l2Sents.size()]);
		List<Pair<String, String>> alignments = align(l1SentsArr, l2SentsArr);

		return alignments;
	}

	
	public List<Pair<String,String>> align(String[] l1Sents, String[] l2Sents) throws AlignerException {

		Logger logger = Logger.getLogger("org.iutools.concordancer.Aligner_Maligna.align");
		logger.trace("#l1 sents="+l1Sents.length+", #l2 sents="+l2Sents.length);
		List<Pair<String,String>> alignments 
				= new ArrayList<Pair<String,String>>();
		
		File tempDir = Files.createTempDir();
		
		writeSentences(l1Sents, l1SentencesFile(tempDir));
		writeSentences(l2Sents, l2SentencesFile(tempDir));
		
		run(l1SentencesFile(tempDir), l2SentencesFile(tempDir));

		List<String> l1AlignedSents = readAlignments(l1SentencesFile(tempDir));
		List<String> l2AlignedSents = readAlignments(l2SentencesFile(tempDir));

		for (int ii = 0; ii < l1AlignedSents.size(); ii++) {
			alignments.add(Pair.of(l1AlignedSents.get(ii), l2AlignedSents.get(ii)));
		}

		logger.trace("Upon exit, #alignments="+alignments.size());
		return alignments;
	}
	
	private List<String> readAlignments(File origFile) throws AlignerException {
		List<String> alignments = new ArrayList<String>();
		File alignmentFile = new File(alignmentsFileFor(origFile.toString()));
		try {
			BufferedReader br = new BufferedReader(new FileReader(alignmentFile));
		    String line;
		    while ((line = br.readLine()) != null) {
		       alignments.add(line);
		    }
		} catch (IOException e) {
			throw new AlignerException(
					"Problem reading alignments file "+alignmentFile, e);
		}
		return alignments;
	}

	private void writeSentences(String[] sentences, File sentsFile) 
					throws AlignerException {
		try {
			FileWriter fw = new FileWriter(sentsFile);
		    for (String aSent: sentences) {
				fw.write(aSent + "\n");
		    }
		    fw.close();				
		} catch (IOException e) {
			throw new AlignerException(
					"Could not write sentences to temporary file "+
					sentsFile.toString());
		}
	}

	public static class LastOriginalLineException extends Exception {
		
	}
	
	private static void usage(String mess) {
		int x = 1;
		if (mess != null) {
			mess = "*** ERROR: "+mess+"\n";
		} else {
			mess = "";
		}
		
		mess += 
			  "\n"
			+ "Usage: Aligner_Maligna sentFile1 sentFile2\n"
			+ "\n"
			+ "   Call a Maligna sequence of commands to alignt two files of sentences."
			;
		System.out.println(mess);
		System.exit(1);
	}	
	
	public static void main(String[] args) throws AlignerException {
		if (args.length != 2) {
			usage("Requires exactly two arguments");
		}
		
		Aligner_Maligna aligner = new Aligner_Maligna();
		aligner.run(args[0], args[1]);
	}

	private void run(File l1SentencesFile, File l2SentencesFile) 
					throws AlignerException {
		run(l1SentencesFile.toString(), l2SentencesFile.toString());	
	}
	
	private void run(String l1SentsFile, String l2SentsFile) 
			throws AlignerException {
		
		ByteArrayOutputStream bOStream = new ByteArrayOutputStream();
		PrintStream output = new PrintStream(bOStream);
		AbstractCommand command = new ParseCommand(output);
		String[] args = new String[] {"-c", "txt", l1SentsFile, l2SentsFile};
		echoCommand(args);
		command.run(args);

		InputStream input = pipeToInputStream(bOStream);
		bOStream = new ByteArrayOutputStream();
		output = new PrintStream(bOStream);
		command = new ModifyCommand(input, output);
		args = new String[] {"-c", "split-sentence"};
		echoCommand(args);
		command.run(args);
		
		input = pipeToInputStream(bOStream);
		bOStream = new ByteArrayOutputStream();
		output = new PrintStream(bOStream);
		command = new ModifyCommand(input, output);
		args = new String[] {"-c", "trim"};
		echoCommand(args);
		command.run(args);
		
		input = pipeToInputStream(bOStream);
		bOStream = new ByteArrayOutputStream();
		output = new PrintStream(bOStream);
		command = new AlignCommand(input, output);
		args = new String[] {"-c", "viterbi", "-a", "poisson", "one-to-one", "-n", "word", "-s", "iterative-band"};
		echoCommand(args);
		command.run(args);
		
		input = pipeToInputStream(bOStream);
		bOStream = new ByteArrayOutputStream();
		output = new PrintStream(bOStream);
		command = new FormatCommand(input, output);
		args = new String[] {"-c", "txt", alignmentsFileFor(l1SentsFile), alignmentsFileFor(l2SentsFile)};
		echoCommand(args);
		command.run(args);
		
		computeSentenceNumbersFile(l1SentsFile);
		computeSentenceNumbersFile(l2SentsFile);
	}

	private void computeSentenceNumbersFile(String origFile) 
			throws AlignerException {
		String alignmentsFile = alignmentsFileFor(origFile);
		
		// Read the two sentence files in parallel
		BufferedReader brOrig;
		try {
			brOrig = new BufferedReader(new FileReader(origFile));
			BufferedReader brAlg = new BufferedReader(new FileReader(alignmentsFile));
			
			List<List<Integer>> sentNumAlignments = new ArrayList<List<Integer>>();
			currOrigLine = 0;
			List<Integer> currPairing = new ArrayList<Integer>();
			while (true) {
				String aligLineContent = brAlg.readLine();
				if (aligLineContent == null) {
					break;
				}

				String origLineContent = null;
				while (origLineContent == null || !origLineContent.equals(aligLineContent)) {
					// The aligned segment is the concatenation of several sentences
					// from the original file.
					//
					// Concatenate the next original sentence and see if we now
					// have a match
					//
					
					try {
						origLineContent = readOrigSentence(brOrig, origLineContent);
					} catch (LastOriginalLineException e) {
						break;
					} catch (IOException e2) {
						throw new AlignerException(e2);
					}
					
					currPairing.add(currOrigLine);				
				}
				
				sentNumAlignments.add(currPairing);
				currPairing = new ArrayList<Integer>();
			}
			
			FileWriter fw = new FileWriter(alignedSentNumsFileFor(origFile));
			ObjectMapper mapper = new ObjectMapper();
			for (List<Integer> alignment: sentNumAlignments) {
				String aligStr = mapper.writeValueAsString(alignment);
				fw.write(aligStr+"\n");
			}
			fw.close();			
		} catch (IOException e1) {
			throw new AlignerException(e1);
		}
	}

	private String readOrigSentence(BufferedReader brOrig, String origLineContent) throws LastOriginalLineException, IOException {
		String nextSent = brOrig.readLine();
		currOrigLine++;		
		if (nextSent == null) {
			throw new LastOriginalLineException();
		}
		
		if (origLineContent == null) {
			origLineContent = "";
		}
		
		String sent = origLineContent + nextSent;
		
		return sent;
	}

	private String alignmentsFileFor(String origFile) {
		String alFile = origFile+".al";
		return alFile;
	}

	private String alignedSentNumsFileFor(String origFile) {
		String alFile = origFile+".al.nums";
		return alFile;
	}

	private InputStream pipeToInputStream(ByteArrayOutputStream output) {
		byte[] bytes = output.toByteArray();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		
		return inputStream;
	}
	
	protected File l1SentencesFile(File tempDir) {
		return new File(tempDir, "l1.sents");
	}
	
	protected File l2SentencesFile(File tempDir) {
		return new File(tempDir, "l2.sents");
	}

	protected File l1AlignmentFile(File tempDir) {
		return new File(tempDir, "l1.al");
	}
	
	protected File l2AlignmentFile(File tempDir) {
		return new File(tempDir, "l2.al");
	}

	protected void echo(String mess) {
		if (verbose) {
			System.out.println(mess);
		}
	}

	protected void echoCommand(String[] args) {
		echo("Running Maligna command: "+String.join(" ", args));
	}
}