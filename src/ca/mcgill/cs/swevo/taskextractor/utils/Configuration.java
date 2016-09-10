package ca.mcgill.cs.swevo.taskextractor.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This is a wrapper for the config.properties file.
 * 
 * @author ctreude
 * 
 */
public final class Configuration
{

	private static Configuration instance = null;

	private String[] aExceptionalMatches;
	private Set<String> aGenericAccusatives;
	private Set<String> aProgrammingActions;
	private Set<String> aPunctuationLemmas;

	private Configuration()
	{
		Properties lProperties = new Properties();
		File lFile = new File("config.properties");
		BufferedReader lBufferedReader;
		try
		{
			lBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(lFile), "UTF8"));
			lProperties.load(lBufferedReader);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		setExceptionalMatches(lProperties.getProperty("EXCEPTIONAL_MATCHES").split(","));
		setGenericAccusatives(new HashSet<String>(Arrays.asList(lProperties.getProperty("GENERIC_ACCUSATIVES").split(
				","))));
		setProgrammingActions(new HashSet<String>(Arrays.asList(lProperties.getProperty("PROGRAMMING_ACTIONS").split(
				","))));
		setPunctuationLemmas(new HashSet<String>(
				Arrays.asList(lProperties.getProperty("PUNCTUATION_LEMMAS").split(","))));
		getPunctuationLemmas().add(",");
	}

	/**
	 * @return singleton
	 */
	public static Configuration getInstance()
	{
		if (instance == null)
		{
			instance = new Configuration();
		}
		return instance;
	}

	/**
	 * @return the aPunctuationLemmas
	 */
	public Set<String> getPunctuationLemmas()
	{
		return aPunctuationLemmas;
	}

	/**
	 * @param pPunctuationLemmas
	 *            the aPunctuationLemmas to set
	 */
	public void setPunctuationLemmas(Set<String> pPunctuationLemmas)
	{
		this.aPunctuationLemmas = pPunctuationLemmas;
	}

	/**
	 * @return the aProgrammingActions
	 */
	public Set<String> getProgrammingActions()
	{
		return aProgrammingActions;
	}

	/**
	 * @param pProgrammingActions
	 *            the aProgrammingActions to set
	 */
	public void setProgrammingActions(Set<String> pProgrammingActions)
	{
		this.aProgrammingActions = pProgrammingActions;
	}

	/**
	 * @return the aGenericAccusatives
	 */
	public Set<String> getGenericAccusatives()
	{
		return aGenericAccusatives;
	}

	/**
	 * @param pGenericAccusatives
	 *            the aGenericAccusatives to set
	 */
	public void setGenericAccusatives(Set<String> pGenericAccusatives)
	{
		this.aGenericAccusatives = pGenericAccusatives;
	}

	/**
	 * @return the aExceptionalMatches
	 */
	public String[] getExceptionalMatches()
	{
		return aExceptionalMatches;
	}

	/**
	 * @param pExceptionalMatches
	 *            the aExceptionalMatches to set
	 */
	public void setExceptionalMatches(String[] pExceptionalMatches)
	{
		this.aExceptionalMatches = pExceptionalMatches;
	}
}
