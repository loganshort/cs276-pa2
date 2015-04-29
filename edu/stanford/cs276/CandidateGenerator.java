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
	public static int INSERT = 0, DELETE = 1, SWAP = 2, TRANS = 3;
	
	public static final Character[] alphabet = {
					'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
					'o','p','q','r','s','t','u','v','w','x','y','z',
					'0','1','2','3','4','5','6','7','8','9',
					' ',',','\''};
	
	// Generate all candidates for the target query
	public Set<Pair<String, Edit>> getCandidates(String query, int distance) throws Exception {
		/*
		 * Your code here
		 */
		Set<Pair<String, Edit>> editone = genEdits(query);
		if (distance == 1) {
			return editone;
		} else {
			Set<Pair<String, Edit>> edittwo = new HashSet<Pair<String, Edit>>();
			for (Pair<String, Edit> edit : editone) {
				edittwo.addAll(genEdits(edit.getFirst()));
			}
			return edittwo;
		}
	}
	
	private boolean validateCandidate(String candidate) {
		if (candidate.length() == 0 ||
			candidate.charAt(candidate.length() - 1) == ' ' ||
			candidate.charAt(0) == ' ') {
			return false;
		}
		String[] words = candidate.split("\\s+");
		for (int i = 0; i < words.length; i++) {
			if (unigram.count(words[i]) == 0) {
				return false;
			}
		}
		return true;
	}
	
	private Edit createEdit(String candidate, int type, char c1, char c2) {
		candidate = candidate.replaceAll("\\s+", " ");
		if (!validateCandidate(candidate)) {
			return null;
		}
		Edit e = new Edit();
		e.type = INSERT;
		e.char1 = c1;
		e.char2 = c2;
		return e;
	}
	private Set<Pair<String, Edit>> genEdits(String query) {
		Set<Pair<String, Edit>> candidates = new HashSet<Pair<String, Edit>>();
		// Addition candidates
		for (int i = 0; i < query.length()+1; i++) {
			for (int j = 0; j < alphabet.length; j++) {
				String candidate = query.substring(0, i) + alphabet[j];
				if (i < query.length()) {
					candidate += query.substring(i);
				}
				char ins;
				if (i == 0) {
					ins = '$';
				} else {
					 ins = query.charAt(i-1);
				}
				Edit e = createEdit(candidate, INSERT, ins, alphabet[j]);
				if (e != null) {
					candidates.add(new Pair<String, Edit>(candidate, e));
				}
			}
		}
		// Deletion candidates
		for (int i = 0; i < query.length(); i++) {
			String candidate = query.substring(0,i);
			if (i+1 < query.length()) {
				candidate += query.substring(i+1);
			}
			char del;
			if (i == 0) {
				del = '$';
			} else {
				 del = query.charAt(i-1);
			}
			Edit e = createEdit(candidate, DELETE, del, query.charAt(i));
			if (e != null) {
				candidates.add(new Pair<String, Edit>(candidate, e));
			}
		}
		// Swap candidates
		for (int i = 0; i < query.length(); i++) {
			for (int j = 0; j < alphabet.length; j++) {
				String candidate = query.substring(0,i) + alphabet[j];
				if (i+1 < query.length()) {
					candidate += query.substring(i+1);
				}
				Edit e = createEdit(candidate, SWAP, alphabet[j], query.charAt(i));
				if (e != null) {
					candidates.add(new Pair<String, Edit>(candidate, e));
				}
			}
		}
		// transpose candidates
		for (int i = 1; i < query.length(); i++) {
			String candidate = query.substring(0,i-1);
			candidate += query.charAt(i) + query.charAt(i-1);
			if (i+1 < query.length()) {
				candidate += query.substring(i+1);
			}
			Edit e = createEdit(candidate, TRANS, query.charAt(i-1), query.charAt(i));
			if (e != null) {
				candidates.add(new Pair<String, Edit>(candidate, e));
			}
		}
		return candidates;
	}
	
	public void loadDictionary(Dictionary unigram) {
		this.unigram = unigram;
	}

}
