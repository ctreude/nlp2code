package ca.mcgill.cs.swevo.taskextractor.analysis;


import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ca.mcgill.cs.swevo.taskextractor.utils.Configuration;
import ca.mcgill.cs.swevo.taskextractor.utils.StringUtils;

/**
 * This class defines the regular expressions used for the detection of code elements.
 * 
 * @author ctreude
 * 
 */
public class CodeElementAnalyzer
{

	private List<SimpleEntry<Pattern, Integer>> aPatterns;
	private List<SimpleEntry<String, Integer>> aRegexes;

	/**
	 * This method returns all patterns.
	 * 
	 * @return list of regular expressions consisting of pattern and group number
	 */
	public List<SimpleEntry<Pattern, Integer>> getPatterns()
	{
		if (aPatterns == null)
		{
			aPatterns = new ArrayList<SimpleEntry<Pattern, Integer>>();
			for (SimpleEntry<String, Integer> lRegex : getRegexes())
			{
				aPatterns.add(new SimpleEntry<Pattern, Integer>(Pattern.compile(lRegex.getKey()), lRegex.getValue()));
			}
		}
		return aPatterns;
	}

	/**
	 * This method determines whether a code element candidate is an exception.
	 * 
	 * @param pCodeElementCandidate
	 *            code element candidate
	 * @return whether or not it is an exception (e.g., because it's a web address rather than a code element)
	 */
	public boolean isCodeElementException(String pCodeElementCandidate)
	{
		if (StringUtils.stringMatchesAny(pCodeElementCandidate, Configuration.getInstance()
						.getExceptionalMatches()))
		{
			return true;
		}
		return false;
	}

	/**
	 * This method creates a new regex entry (regex + the group number that's actually the code element).
	 * 
	 * @param pRegex
	 *            the regular expression
	 * @param pGroupNumber
	 *            the group number
	 * @return regex entry (regex + group number)
	 */
	private SimpleEntry<String, Integer> createRegexEntry(String pRegex, int pGroupNumber)
	{
		return new SimpleEntry<String, Integer>(pRegex, new Integer(pGroupNumber));
	}

	/**
	 * This method creates all regexes if they haven't been created yet.
	 * 
	 * @return list of regex entries (regex + group number)
	 */
	private List<SimpleEntry<String, Integer>> getRegexes()
	{
		if (aRegexes == null)
		{
			aRegexes = new ArrayList<SimpleEntry<String, Integer>>();
			aRegexes.add(createRegexEntry("([A-Z][a-zA-Z]+ ?<[A-Z][a-zA-Z]*>)", 0));
			aRegexes.add(createRegexEntry("([a-zA-Z0-9\\.]+[(][a-zA-Z_,\\. ]*[)])", 0));
			aRegexes.add(createRegexEntry(
					"(https?://)?[a-zA-Z_\\-/]{2,}(\\.[a-zA-Z_0-9\\-]{2,})+[^\\s\\<\\>{\\(\\),'\"��}:]*", 0));
			aRegexes.add(createRegexEntry("([\\.]?[/]?\\w+\\.\\w+\\.?\\w+(?:\\.\\w+)*)", 0));
			aRegexes.add(createRegexEntry("([A-Za-z]+\\.[A-Z]+)", 0));
			aRegexes.add(createRegexEntry("([@][a-zA-Z]+)", 0));
			aRegexes.add(createRegexEntry("(?:\\s|^)([a-zA-z]{3,}\\.[A-Za-z]+_[a-zA-Z_]+)", 1));
			aRegexes.add(createRegexEntry("\\b([A-Z]{2,})\\b", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([A-Z]+_[A-Z0-9_]+)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([a-z]+_[a-z0-9_]+)", 1));
			aRegexes.add(createRegexEntry("(\\w{3,}:\\w+[a-zA-Z0-9:]*)", 0));
			aRegexes.add(createRegexEntry("(?:\\s|^)([A-Z]+[a-z0-9]+[A-Z][a-z0-9]+\\w*)(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([A-Z]{3,}[a-z0-9]{2,}\\w*)(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([a-z0-9]+[A-Z]+\\w*)(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)(\\w+\\([^)]*\\))(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("([A-Z][a-z]+[A-Z][a-zA-Z]+)(\\s|,|\\.|\\))", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([a-z]+[A-Z][a-zA-Z]+)(\\s|,|\\.|\\))", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([A-Z]+[a-z0-9]+[A-Z][a-z0-9]+\\w*)(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([A-Z]{3,}[a-z0-9]{2,}\\w*)(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)([a-z0-9]+[A-Z]+\\w*)(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("(?:\\s|^)(\\w+\\([^)]*\\))(\\s|\\.\\s|\\.$|$|,\\s)", 1));
			aRegexes.add(createRegexEntry("([A-Z][a-z]+[A-Z][a-zA-Z]+)(\\s|,|\\.|\\))", 1));
			aRegexes.add(createRegexEntry("([a-z]+[A-Z][a-zA-Z]+)(\\s|,|\\.|\\))", 1));
			aRegexes.add(createRegexEntry("([a-z] )([A-Z][a-z]{3,11})( )", 2));
			aRegexes.add(createRegexEntry("(</?[a-zA-Z0-9 ]+>)", 0));
			aRegexes.add(createRegexEntry("(\\{\\{[^\\}]*\\}\\})", 0));
			aRegexes.add(createRegexEntry("(\\{\\%[^\\%]*\\%\\})", 0));
			aRegexes.add(createRegexEntry("(/[^/]*/)", 0));
			aRegexes.add(createRegexEntry("(�[^�]*�)", 0));
			aRegexes.add(createRegexEntry("(__[^_]*__)", 0));
			aRegexes.add(createRegexEntry("(\\$[A-Za-z\\_]+)", 0));
			aRegexes.add(createRegexEntry("\\b(null)\\b", 0));
			aRegexes.add(createRegexEntry("\\b(static)\\b", 0));
			aRegexes.add(createRegexEntry("\\b(true)\\b", 0));
			aRegexes.add(createRegexEntry("\\b(false)\\b", 0));
			aRegexes.add(createRegexEntry("\\b(float)\\b", 0));
		}
		return aRegexes;
	}

}
