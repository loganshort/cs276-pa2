package edu.stanford.cs276;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;

public class CandidateGenerator implements Serializable {

	private Dictionary unigram = null;
	private Dictionary bigram = null;

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
	public Set<Pair<String, List<Edit>>> getCandidates(String query, int distance) throws Exception {
		/*
		 * Your code here
		 */
		Set<Pair<String, List<Edit>>> editone = genEdits(new Pair<String, List<Edit>>(query, new ArrayList<Edit>()));
		if (distance == 1) {
			return editone;
		} else {
			Set<Pair<String, List<Edit>>> edittwo = new HashSet<Pair<String, List<Edit>>>();
			for (Pair<String, List<Edit>> edit : editone) {
				edittwo.addAll(genEdits(edit));
			}
			return edittwo;
		}
	}
	
	private int badWords(String query) {
		String[] words = query.split(" ");
		int numBads = 0;
		for (int i = 0; i < words.length; i++) {
			if (unigram.count(words[i]) == 0) {
				numBads++;
			}
		}
		return numBads;
	}
	
	private boolean validateCandidate(String candidate, int originalBads, char first, char orig, char edit) {
		if (candidate.length() == 0 ||
			candidate.charAt(candidate.length() - 1) == ' ' ||
			candidate.charAt(0) == ' ') {
			return false;
		}
		int numBads = 0;
		String[] words = candidate.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (unigram.count(words[i]) == 0) {
				numBads++;
			}
		}
		if (numBads >= originalBads && numBads > 0) {
			return false;
		} else if (numBads == 1 && originalBads == 2) {
			if (edit == '\0') {
				if (bigram.count(""+orig+first) < bigram.count(""+first+orig)) return false;
			} else {
				if (bigram.count(""+first+orig) < bigram.count(""+first+edit)) return false;
			}
		}
		return true;
	}
	
	private Edit createEdit(String candidate, int type, char c1, char c2) {
		Edit e = new Edit();
		e.type = type;
		e.char1 = c1;
		e.char2 = c2;
		return e;
	}
	
	private Set<Pair<String, List<Edit>>> genEdits(Pair<String, List<Edit>> query) {
		Set<String> queries = new HashSet<String>();
		Set<Pair<String, List<Edit>>> candidates = new HashSet<Pair<String, List<Edit>>>();
		String querystr = query.getFirst();
		int numBads = badWords(querystr);
		// Addition candidates
		for (int i = 0; i < querystr.length()+1; i++) {
			for (int j = 0; j < alphabet.length; j++) {
				String candidate = querystr.substring(0, i) + alphabet[j];
				if (i < querystr.length()) {
					candidate += querystr.substring(i);
				}
				char ins;
				if (i == 0) ins = '$';
				else ins = querystr.charAt(i-1);
				char end;
				if (i == querystr.length()) end = '$';
				else end = querystr.charAt(i);
				if (!queries.contains(candidate) && validateCandidate(candidate, numBads, ins, end, alphabet[j])) {
					queries.add(candidate);
					Edit e = createEdit(candidate, INSERT, ins, alphabet[j]);
					List<Edit> edits = new ArrayList<Edit>(query.getSecond());
					edits.add(e);
					candidates.add(new Pair<String, List<Edit>>(candidate, edits));
				}
			}
		}
		// Deletion candidates
		for (int i = 0; i < querystr.length(); i++) {
			String candidate = querystr.substring(0,i);
			if (i+1 < querystr.length()) {
				candidate += querystr.substring(i+1);
			}
			char del;
			if (i == 0) del = '$';
			else del = querystr.charAt(i-1);
			char end;
			if (i+1 == querystr.length()) end = '$';
			else end = querystr.charAt(i);
			if (!queries.contains(candidate) && validateCandidate(candidate, numBads, del, querystr.charAt(i), end)) {
				queries.add(candidate);
				Edit e = createEdit(candidate, DELETE, del, querystr.charAt(i));
				List<Edit> edits = new ArrayList<Edit>(query.getSecond());
				edits.add(e);
				candidates.add(new Pair<String, List<Edit>>(candidate, edits));
			}
		}
		// Swap candidates
		for (int i = 0; i < querystr.length(); i++) {
			for (int j = 0; j < alphabet.length; j++) {
				String candidate = querystr.substring(0,i) + alphabet[j];
				if (i+1 < querystr.length()) {
					candidate += querystr.substring(i+1);
				}
				char first;
				if (i == 0) {
					first = '$';
				} else {
					 first = querystr.charAt(i-1);
				}
				if (!queries.contains(candidate) && validateCandidate(candidate, numBads, first, querystr.charAt(i), alphabet[j])) {
					queries.add(candidate);
					Edit e = createEdit(candidate, SWAP, alphabet[j], querystr.charAt(i));
					List<Edit> edits = new ArrayList<Edit>(query.getSecond());
					edits.add(e);
					candidates.add(new Pair<String, List<Edit>>(candidate, edits));
				}
			}
		}
		// transpose candidates
		for (int i = 1; i < querystr.length(); i++) {
			String candidate = querystr.substring(0,i-1);
			candidate += querystr.substring(i,i+1) + querystr.substring(i-1,i);
			if (i+1 < querystr.length()) {
				candidate += querystr.substring(i+1);
			}
			if (!queries.contains(candidate) && validateCandidate(candidate, numBads, querystr.charAt(i-1), querystr.charAt(i), '\0')) {
				queries.add(candidate);
				Edit e = createEdit(candidate, TRANS, querystr.charAt(i-1), querystr.charAt(i));
				List<Edit> edits = new ArrayList<Edit>(query.getSecond());
				edits.add(e);
				candidates.add(new Pair<String, List<Edit>>(candidate, edits));
			}
		}
		return candidates;
	}
	
	public void loadDictionary(Dictionary unigram, Dictionary bigram) {
		this.unigram = unigram;
		this.bigram = bigram;
	}

}
