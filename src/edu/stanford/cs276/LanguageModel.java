package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;

import java.lang.Math;



public class LanguageModel implements Serializable {

	private static LanguageModel lm_;
	/* Feel free to add more members here.
	 * You need to implement more methods here as needed.
	 * 
	 * Your code here ...
	 */
	private static final double LAMBDA = 0.1;
	private static final double LOG_ZERO = -100;
	
	Dictionary unigram = new Dictionary();
	Dictionary bigram = new Dictionary();
	
	private double getUnigramProb(String term) {
		return (1.0*unigram.count(term))/(1.0*unigram.termCount());
	}
	
	/**
	 * Takes as input a pair (w_1, w_2) and computes P(w_2|w_1)
	 * @param pair
	 */
	private double getBigramProb(Pair<String, String> pair) {
		if (unigram.count(pair.getFirst()) == 0) {
			return 0;
		}
		return (1.0*bigram.count(pair.toString()))/(1.0*unigram.count(pair.getFirst()));
	}
	
	/**
	 * Takes as input a pair (w_1, w_2) and computes the log interpolated P(w_2|w_1)
	 * @param pair
	 */
	private double getInterProb(Pair<String, String> pair) {
		double likelihood = LAMBDA*(getUnigramProb(pair.getSecond()))
				+ (1-LAMBDA)*(getBigramProb(pair));
		if (likelihood == 0) {
			return LOG_ZERO;
		}
		return Math.log(LAMBDA*(getUnigramProb(pair.getSecond()))
				+ (1-LAMBDA)*(getBigramProb(pair)));
	}
	
	/**
	 * Takes a query and computes the log likelihood
	 * @param query
	 */
	public double getQueryProb(String query) {
		String[] words = query.trim().split("\\s+");
		double loglike = 0;
		if (words.length > 0) {
			double uniP = getUnigramProb(words[0]);
			if (uniP != 0) {
				loglike += Math.log(uniP);
			} else {
				loglike += LOG_ZERO;
			}
			for(int i = 0; i < words.length-1; i++) {
				loglike += getInterProb(new Pair<String, String>(words[i],words[i+1]));
			}
		}
		return loglike;
	}
	
	// Do not call constructor directly since this is a Singleton
	private LanguageModel(String corpusFilePath) throws Exception {
		constructDictionaries(corpusFilePath);
	}


	public void constructDictionaries(String corpusFilePath)
			throws Exception {

		System.out.println("Constructing dictionaries...");
		File dir = new File(corpusFilePath);
		for (File file : dir.listFiles()) {
			if (".".equals(file.getName()) || "..".equals(file.getName())) {
				continue; // Ignore the self and parent aliases.
			}
			System.out.printf("Reading data file %s ...\n", file.getName());
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = input.readLine()) != null) {
				/*
				 * Your code here
				 */
				String[] words = line.trim().split("\\s+");
				for(int i = 0; i < words.length; i++) {
					unigram.add(words[i]);
					if (i < words.length - 1) {
						bigram.add(new Pair<String, String>(words[i], words[i+1]).toString());
					}
				}
			}
			input.close();
		}
		System.out.println("Done.");
	}
	
	// Loads the object (and all associated data) from disk
	public static LanguageModel load() throws Exception {
		try {
			if (lm_==null){
				FileInputStream fiA = new FileInputStream(Config.languageModelFile);
				ObjectInputStream oisA = new ObjectInputStream(fiA);
				lm_ = (LanguageModel) oisA.readObject();
			}
		} catch (Exception e){
			throw new Exception("Unable to load language model.  You may have not run build corrector");
		}
		return lm_;
	}
	
	// Saves the object (and all associated data) to disk
	public void save() throws Exception{
		FileOutputStream saveFile = new FileOutputStream(Config.languageModelFile);
		ObjectOutputStream save = new ObjectOutputStream(saveFile);
		save.writeObject(this);
		save.close();
	}
	
	// Creates a new lm object from a corpus
	public static LanguageModel create(String corpusFilePath) throws Exception {
		if(lm_ == null ){
			lm_ = new LanguageModel(corpusFilePath);
		}
		return lm_;
	}
}
