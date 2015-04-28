package edu.stanford.cs276;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;

public class CandidateGenerator implements Serializable {

	private Dictionary unigram = null;
	private static CandidateGenerator cg_;
	
	// Don't use the constructor since this is a Singleton instance
	private CandidateGenerator() {}
	
	public static CandidateGenerator get() throws Exception{
		if (cg_ == null ){
			cg_ = new CandidateGenerator();
		}
		return cg_;
	}
	
	
	public static final Character[] alphabet = {
					'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
					'o','p','q','r','s','t','u','v','w','x','y','z',
					'0','1','2','3','4','5','6','7','8','9',
					' ',',','\''};
	
	// Generate all candidates for the target query
	public Set<String> getCandidates(String query, int distance) throws Exception {
		/*
		 * Your code here
		 */
		Set<String> editone = genEdits(query);
		if (distance == 1) {
			return editone;
		} else {
			Set<String> edittwo = new HashSet<String>();
			for (String edit : editone) {
				edittwo.addAll(genEdits(edit));
			}
			return edittwo;
		}
	}
	
	private boolean validateCandidate(String candidate) {
		String[] words = candidate.trim().split("\\s+");
		for (int i = 0; i < words.length; i++) {
			if (unigram.count(words[i]) == 0) {
				return false;
			}
		}
		return true;
	}
	
	private Set<String> genEdits(String query) {
		Set<String> candidates = new HashSet<String>();	
		// Addition candidates
		for (int i = 0; i < query.length()+1; i++) {
			for (int j = 0; j < alphabet.length; j++) {
				String candidate = query.substring(0, i) + alphabet[j];
				if (i < query.length()) {
					candidate += query.substring(i);
				}
				candidate = candidate.trim().replaceAll("\\s+", " ");
				if (validateCandidate(candidate)) {
					candidates.add(candidate);
				}
			}
		}
		// Deletion candidates
		for (int i = 0; i < query.length(); i++) {
			String candidate = query.substring(0,i);
			if (i+1 < query.length()) {
				candidate += query.substring(i+1);
			}
			candidate = candidate.trim().replaceAll("\\s+", " ");
			if (validateCandidate(candidate)) {
				candidates.add(candidate);
			}
		}
		// Swap candidates
		for (int i = 0; i < query.length(); i++) {
			for (int j = 0; j < alphabet.length; j++) {
				String candidate = query.substring(0,i) + alphabet[j];
				if (i+1 < query.length()) {
					candidate += query.substring(i+1);
				}
				candidate = candidate.trim().replaceAll("\\s+", " ");
				if (validateCandidate(candidate)) {
					candidates.add(candidate);
				}
			}
		}		
		return candidates;
	}
	
	public void loadDictionary(Dictionary unigram) {
		this.unigram = unigram;
	}

}
