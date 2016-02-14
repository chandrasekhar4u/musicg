/*
 *
 */
package com.musicg.math.rank;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapRankDouble implements MapRank {

	@SuppressWarnings("rawtypes")
	private final Map map;
	private boolean acsending = true;

	public MapRankDouble(final Map<?, Double> map, final boolean acsending) {
		this.map = map;
		this.acsending = acsending;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getOrderedKeyList(int numKeys, final boolean sharpLimit) { // if sharp limited, will return sharp numKeys, otherwise will return until the values not equals the exact key's value

		final Set mapEntrySet = map.entrySet();
		final List keyList = new LinkedList();

		// if the numKeys is larger than map size, limit it
		if (numKeys > map.size()) {
			numKeys = map.size();
		}
		// end if the numKeys is larger than map size, limit it

		if (map.size() > 0) {
			final double[] array = new double[map.size()];
			int count = 0;

			// get the pass values
			Iterator<Entry> mapIterator = mapEntrySet.iterator();
			while (mapIterator.hasNext()) {
				final Entry entry = mapIterator.next();
				array[count++] = (Double) entry.getValue();
			}
			// end get the pass values

			int targetindex;
			if (acsending) {
				targetindex = numKeys;
			} else {
				targetindex = array.length - numKeys;
			}

			final double passValue = getOrderedValue(array, targetindex); // this value is the value of the numKey-th element
			// get the passed keys and values
			final Map passedMap = new HashMap();
			final List<Double> valueList = new LinkedList<Double>();
			mapIterator = mapEntrySet.iterator();

			while (mapIterator.hasNext()) {
				final Entry entry = mapIterator.next();
				final double value = (Double) entry.getValue();
				if ((acsending && value <= passValue) || (!acsending && value >= passValue)) {
					passedMap.put(entry.getKey(), value);
					valueList.add(value);
				}
			}
			// end get the passed keys and values

			// sort the value list
			final Double[] listArr = new Double[valueList.size()];
			valueList.toArray(listArr);
			Arrays.sort(listArr);
			// end sort the value list

			// get the list of keys
			int resultCount = 0;
			int index;
			if (acsending) {
				index = 0;
			} else {
				index = listArr.length - 1;
			}

			if (!sharpLimit) {
				numKeys = listArr.length;
			}

			while (true) {
				final double targetValue = listArr[index];
				final Iterator<Entry> passedMapIterator = passedMap.entrySet().iterator();
				while (passedMapIterator.hasNext()) {
					final Entry entry = passedMapIterator.next();
					if ((Double) entry.getValue() == targetValue) {
						keyList.add(entry.getKey());
						passedMapIterator.remove();
						resultCount++;
						break;
					}
				}

				if (acsending) {
					index++;
				} else {
					index--;
				}

				if (resultCount >= numKeys) {
					break;
				}
			}
			// end get the list of keys
		}

		return keyList;
	}

	private double getOrderedValue(final double[] array, final int index) {
		locate(array, 0, array.length - 1, index);
		return array[index];
	}

	// sort the partitions by quick sort, and locate the target index
	private void locate(final double[] array, final int left, final int right, final int index) {

		final int mid = (left + right) / 2;
		//System.out.println(left+" to "+right+" ("+mid+")");

		if (right == left) {
			//System.out.println("* "+array[targetIndex]);
			//result=array[targetIndex];
			return;
		}

		if (left < right) {
			final double s = array[mid];
			int i = left - 1;
			int j = right + 1;

			while (true) {
				while (array[++i] < s) {
					;
				}
				while (array[--j] > s) {
					;
				}
				if (i >= j) {
					break;
				}
				swap(array, i, j);
			}

			//System.out.println("2 parts: "+left+"-"+(i-1)+" and "+(j+1)+"-"+right);

			if (i > index) {
				// the target index in the left partition
				//System.out.println("left partition");
				locate(array, left, i - 1, index);
			} else {
				// the target index in the right partition
				//System.out.println("right partition");
				locate(array, j + 1, right, index);
			}
		}
	}

	private void swap(final double[] array, final int i, final int j) {
		final double t = array[i];
		array[i] = array[j];
		array[j] = t;
	}
}