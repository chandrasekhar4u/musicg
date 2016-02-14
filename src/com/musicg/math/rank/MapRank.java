/*
 *
 */
package com.musicg.math.rank;

import java.util.List;

public interface MapRank {
	@SuppressWarnings("rawtypes")
	public List getOrderedKeyList(int numKeys, boolean sharpLimit);
}