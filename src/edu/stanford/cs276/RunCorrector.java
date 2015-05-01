package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.stanford.cs276.util.Pair;

public class RunCorrector {

	private static final double MU = 1;
	
	public static LanguageModel languageModel;
	public static NoisyChannelModel nsm;
	public static CandidateGenerator cg;
	

	public static void main(String[] args) throws Exception {
		
		long startTime = System.currentTimeMillis();
		
		// Parse input arguments
		String uniformOrEmpirical = null;
		String queryFilePath = null;
		String goldFilePath = null;
		String extra = null;
		BufferedReader goldFileReader = null;
		if (args.length == 2) {
			// Run without extra and comparing to gold
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
		}
		else if (args.length == 3) {
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
			if (args[2].equals("extra")) {
				extra = args[2];
			} else {
				goldFilePath = args[2];
			}
		} 
		else if (args.length == 4) {
			uniformOrEmpirical = args[0];
			queryFilePath = args[1];
			extra = args[2];
			goldFilePath = args[3];
		}
		else {
			System.err.println(
					"Invalid arguments.  Argument count must be 2, 3 or 4" +
					"./runcorrector <uniform | empirical> <query file> \n" + 
					"./runcorrector <uniform | empirical> <query file> <gold file> \n" +
					"./runcorrector <uniform | empirical> <query file> <extra> \n" +
					"./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt extra \n" +
					"SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
			return;
		}
		
		if (goldFilePath != null ){
			goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
		}
		
		// Load models from disk
		languageModel = LanguageModel.load(); 
		nsm = NoisyChannelModel.load();
		cg = CandidateGenerator.get();
		cg.loadDictionary(languageModel.unigram, languageModel.bigram);
		BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
		nsm.setProbabilityType(uniformOrEmpirical);
		
		int totalCount = 0;
		int yourCorrectCount = 0;
		String query = null;		
		
		/*
		 * Each line in the file represents one query.  We loop over each query and find
		 * the most likely correction
		 */
		while ((query = queriesFileReader.readLine()) != null) {
			String correctedQuery = query;
			/*
			 * Your code here
			 */
			double bestLikelihood = Math.log(nsm.getLikelihood(query, new ArrayList<Edit>())) +
				MU*languageModel.getQueryProb(query, "extra".equals(extra));
			// Edit distance 1 candidates
			Set<Pair<String, List<Edit>>> candidates = cg.getCandidates(query, 1);
			for (Pair<String, List<Edit>> candidate : candidates) {
				double likelihood = Math.log(nsm.getLikelihood(candidate.getFirst(), candidate.getSecond()));
				likelihood += MU*languageModel.getQueryProb(candidate.getFirst(), "extra".equals(extra));
				if (likelihood > bestLikelihood) {
					correctedQuery = candidate.getFirst();
					bestLikelihood = likelihood;
				}
			}
			// Edit distance 2 candidates
			candidates = cg.getCandidates(query, 2);
			for (Pair<String, List<Edit>> candidate : candidates) {
				double likelihood = Math.log(nsm.getLikelihood(candidate.getFirst(), candidate.getSecond()));
				likelihood += MU*languageModel.getQueryProb(candidate.getFirst(), "extra".equals(extra));
				if (likelihood > bestLikelihood) {
					correctedQuery = candidate.getFirst();
					bestLikelihood = likelihood;
				}
			}
			
			
			if ("extra".equals(extra)) {
				/*
				 * If you are going to implement something regarding to running the corrector, 
				 * you can add code here. Feel free to move this code block to wherever 
				 * you think is appropriate. But make sure if you add "extra" parameter, 
				 * it will run code for your extra credit and it will run you basic 
				 * implementations without the "extra" parameter.
				 */	
			}
			

			// If a gold file was provided, compare our correction to the gold correction
			// and output the running accuracy
			String goldQuery = "";
			if (goldFileReader != null) {
				goldQuery = goldFileReader.readLine();
				if (goldQuery.equals(correctedQuery)) {
					yourCorrectCount++;
				}
				totalCount++;
			}
			System.out.println(correctedQuery);
		}
		queriesFileReader.close();
		/*long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println((yourCorrectCount*1.0)/(totalCount*1.0));
		System.out.println("RUNNING TIME: "+totalTime/1000+" seconds ");*/
	}
}
