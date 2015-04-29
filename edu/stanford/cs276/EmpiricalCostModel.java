package edu.stanford.cs276;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import edu.stanford.cs276.util.Dictionary;
import edu.stanford.cs276.util.Pair;

public class EmpiricalCostModel implements EditCostModel{
	
	private Dictionary types;
	private Dictionary counts;
	public static int INSERT = 0, DELETE = 1, SWAP = 2, TRANS = 3;
	private static final double EDIT_PROB = 0.01;
	private static final double MATCH_PROB = 0.95;
	public static final Character[] alphabet = {
		'a','b','c','d','e','f','g','h','i','j','k','l','m','n',
		'o','p','q','r','s','t','u','v','w','x','y','z',
		'0','1','2','3','4','5','6','7','8','9',
		' ',',','\''};
	
	public EmpiricalCostModel(String editsFile) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(editsFile));
		System.out.println("Constructing edit distance map...");
		counts = new Dictionary();
		types = new Dictionary();
		String line = null;
		while ((line = input.readLine()) != null) {
			Scanner lineSc = new Scanner(line);
			lineSc.useDelimiter("\t");
			String noisy = lineSc.next();
			String clean = lineSc.next();
			
			if (noisy.equals(clean)) {
				continue;
			}
			
			counts.add("$" + clean.charAt(0));
			for (int i = 0; i < clean.length(); i++) {
				counts.add(clean.substring(i, i+1));
				if (i+1 < clean.length()) {
					counts.add(clean.substring(i, i+2));
				}
			}
			String edit = getEdits(clean, noisy);
			if (edit.isEmpty()) System.out.println("NO EDIT FOUND");
			types.add(edit);
			lineSc.close();
		}

		input.close();
		System.out.println("Done.");
	}
	
	public String getEdits(String clean, String noisy) {
		String edit = "";
		char prev = '$';
		int len = clean.length();
		if (noisy.length() > len && clean.equals(noisy.substring(0, len))) {
			return edit + INSERT + clean.charAt(len-1) + noisy.charAt(len);
		} else if (len > noisy.length() && noisy.equals(clean.substring(0, len-1))) {
			return edit + DELETE + clean.charAt(len-2) + clean.charAt(len-1);
		}
		for (int i = 0; i < clean.length(); i++) {
			if (clean.substring(i).equals(noisy.substring(i+1))) {
				return edit + INSERT + prev + noisy.charAt(i);
			} else if (clean.substring(i+1).equals(noisy.substring(i))) {
				return edit + DELETE + prev + clean.charAt(i);
			} else if (clean.charAt(i) != noisy.charAt(i)) {
				if (clean.substring(i+1).equals(noisy.substring(i+1))) {
					return edit + SWAP + noisy.charAt(i) + clean.charAt(i);
				} else {
					return edit + TRANS + clean.charAt(i) + clean.charAt(i+1);
				}
			}
			prev = clean.charAt(i);
		}
		return edit;
	}
	
	// You need to update this to calculate the proper empirical cost
	@Override
	public double editProbability(String original, Pair<String, Edit> R, int distance) {
		if (original.equals(R.getFirst())) {
			if (distance == 0) {
				return MATCH_PROB;
			}
			return Math.pow(EDIT_PROB, distance);
		}
		Edit e = R.getSecond();
		String c;
		if (e.type == INSERT) {
			c = "" + e.char1;
		} else if (e.type == SWAP) {
			c = "" + e.char2;
		} else {
			c = "" + e.char1 + e.char2;
		}
		double prob = (types.count("" + e.type + e.char1 + e.char2)+1)*1.0 / (counts.count(c) + alphabet.length);
		return Math.pow(prob, distance);
	}
}
