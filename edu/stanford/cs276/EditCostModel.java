package edu.stanford.cs276;

import java.io.Serializable;

import edu.stanford.cs276.util.Pair;

public interface EditCostModel extends Serializable {

	public double editProbability(String original, Pair<String, Edit> R, int distance);
}
