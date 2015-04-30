package edu.stanford.cs276;

import java.lang.Math;
import java.util.List;

import edu.stanford.cs276.util.Pair;

public class UniformCostModel implements EditCostModel {
	
	private static final double EDIT_PROB = 0.01;
	private static final double MATCH_PROB = 0.95;
	
	@Override
	public double editProbability(String R, List<Edit> edits) {
		/*
		 * Your code here
		 */
		int distance = edits.size();
		if (distance == 0) {
			return MATCH_PROB;
		}
		return Math.pow(EDIT_PROB, distance);
	}
}
