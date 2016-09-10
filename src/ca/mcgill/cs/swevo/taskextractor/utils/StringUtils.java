package ca.mcgill.cs.swevo.taskextractor.utils;


import java.util.Set;

/**
 * Utilities for strings.
 * 
 * @author ctreude
 * 
 */
public final class StringUtils
{

	private StringUtils()
	{

	}

	/**
	 * @param pString
	 *            string
	 * @param pPrefixes
	 *            set of prefixes
	 * @return whether string starts with any of the prefixes (case insensitive)
	 */
	public static boolean startsWithAnyIgnoreCase(String pString, Set<String> pPrefixes)
	{
		for (String lPrefix : pPrefixes)
		{
			if (pString.toLowerCase().startsWith(lPrefix.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pString
	 *            string
	 * @param pContains
	 *            set of substrings
	 * @return whether string contains any of the substrings
	 */
	public static boolean stringContainsAny(String pString, String[] pContains)
	{
		for (String lContains : pContains)
		{
			if (pString.contains(lContains))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pString
	 *            string
	 * @param pEndings
	 *            set of suffixes
	 * @return whether string ends with any of the suffixes
	 */
	public static boolean stringEndsWithAny(String pString, String[] pEndings)
	{
		for (String lEnding : pEndings)
		{
			if (pString.endsWith(lEnding))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pString
	 *            string
	 * @param pEquals
	 *            set of strings
	 * @return whether string equals any of the other strings
	 */
	public static boolean stringEqualsAny(String pString, String[] pEquals)
	{
		for (String lEqual : pEquals)
		{
			if (pString.equals(lEqual))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pString
	 *            string
	 * @param pMatches
	 *            set of regexes
	 * @return whether string matches any of the regexes
	 */
	public static boolean stringMatchesAny(String pString, String[] pMatches)
	{
		for (String lMatch : pMatches)
		{
			if (pString.matches(lMatch))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param pString
	 *            string
	 * @param pStarts
	 *            set of prefixes
	 * @return whether string starts with any of the prefixes
	 * @see stringStartsWithAnyIgnoreCase
	 */
	public static boolean stringStartsWithAny(String pString, String[] pStarts)
	{
		for (String lStart : pStarts)
		{
			if (pString.startsWith(lStart))
			{
				return true;
			}
		}
		return false;
	}

}
