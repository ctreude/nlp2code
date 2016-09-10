package ca.mcgill.cs.swevo.taskextractor.utils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class offers utilities for maps.
 * 
 * @author ctreude
 * 
 */
public final class MapUtils
{

	/**
	 * Comparator to sort map by value.
	 * 
	 * @author ctreude
	 * 
	 * @param <K>
	 *            key
	 * @param <V>
	 *            value
	 */
	private static class ByValueAsc<K, V extends Comparable<V>> implements Comparator<Entry<K, V>>
	{
		public int compare(Entry<K, V> pO1, Entry<K, V> pO2)
		{
			return pO1.getValue().compareTo(pO2.getValue());
		}
	}

	private MapUtils()
	{

	}

	/**
	 * Sorts map by value.
	 * 
	 * @param pMap
	 *            map to be sorted by value
	 * @param <K>
	 *            key
	 * @param <V>
	 *            value
	 * @return list with map entries sorted by value
	 */
	public static <K, V extends Comparable<V>> List<Entry<K, V>> sortByValueAsc(Map<K, V> pMap)
	{
		List<Entry<K, V>> lEntries = new ArrayList<Entry<K, V>>(pMap.entrySet());
		Collections.sort(lEntries, new ByValueAsc<K, V>());
		return lEntries;
	}
}
