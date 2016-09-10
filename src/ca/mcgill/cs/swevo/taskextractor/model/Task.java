package ca.mcgill.cs.swevo.taskextractor.model;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;

import edu.stanford.nlp.ling.IndexedWord;

/**
 * Representation of a task.
 * 
 * @author ctreude
 * 
 */
public class Task
{
	private List<IndexedWord> aAccusative;
	private String aPreposition;
	private List<IndexedWord> aPrepositionObject;
	private List<IndexedWord> aVerb;
	private String aString = null;

	/**
	 * @param pTasks
	 *            list of tasks
	 * @return whether this task is contained in list of tasks
	 */
	public boolean containedIn(List<Task> pTasks)
	{
		for (Task lTask : pTasks)
		{
			if (equalsTask(lTask))
			{
				return true;
			}
		}
		return false;

	}

	/**
	 * @return short accusative
	 */
	public List<IndexedWord> getAccusative()
	{
		return aAccusative;
	}

	/**
	 * @return short prep object
	 */
	public List<IndexedWord> getPrepositionObject()
	{
		return aPrepositionObject;
	}

	/**
	 * @return short verb
	 */
	public List<IndexedWord> getVerb()
	{
		return aVerb;
	}

	/**
	 * @param pAccusative
	 *            short accusative
	 */
	public void setAccusative(List<IndexedWord> pAccusative)
	{
		aAccusative = pAccusative;
	}

	/**
	 * @param pPreposition
	 *            preposition
	 */
	public void setPreposition(String pPreposition)
	{
		if (pPreposition == null)
		{
			aPreposition = "";
		}
		else
		{
			aPreposition = pPreposition;
		}
	}

	/**
	 * @param pPrepositionObject
	 *            short prep object
	 */
	public void setPrepositionObject(List<IndexedWord> pPrepositionObject)
	{
		this.aPrepositionObject = pPrepositionObject;
	}

	/**
	 * @param pVerb
	 *            short verb
	 */
	public void setVerb(List<IndexedWord> pVerb)
	{
		this.aVerb = pVerb;
	}

	/**
	 * @return string representation of task
	 */
	public String toString()
	{
		if (aString != null) {
			return aString;
		}
		String lString = getLemmaWords(aVerb) + " " + getLowerCaseWords(aAccusative) + " " + aPreposition + " "
				+ getLowerCaseWords(aPrepositionObject);
		lString = lString.trim();
		Pattern lPattern = Pattern.compile("ce[0-9]+");
		Matcher lMatcher = lPattern.matcher(lString);
		while (lMatcher.find())
		{
			String lUnmasked = CodeElementDictionary.getCodeElementForMask(lMatcher.group());
			lString = lString.replace(lMatcher.group(), lUnmasked.replace("\"", "\\\""));
		}
		aString = lString;
		return lString;
	}

	private boolean equalsTask(Task pTask)
	{
		if (!ObjectUtils.equals(pTask.getAccusative(), aAccusative))
		{
			return false;
		}
		if (!ObjectUtils.equals(pTask.getVerb(), aVerb))
		{
			return false;
		}
		if (!ObjectUtils.equals(pTask.getPreposition(), aPreposition)
				|| !ObjectUtils.equals(pTask.getPrepositionObject(), aPrepositionObject))
		{
			return false;
		}
		return true;
	}

	private String getLemmaWords(List<IndexedWord> pIndexedWords)
	{
		if (pIndexedWords == null)
		{
			return "";
		}
		String lString = "";
		for (IndexedWord lIndexedWord : pIndexedWords)
		{
			lString += lIndexedWord.lemma().toLowerCase() + " ";
		}
		return lString.trim();
	}

	private String getLowerCaseWords(List<IndexedWord> pIndexedWords)
	{
		if (pIndexedWords == null)
		{
			return "";
		}
		String lString = "";
		for (IndexedWord lIndexedWord : pIndexedWords)
		{
			lString += lIndexedWord.word().toLowerCase() + " ";
		}
		return lString.trim();
	}

	private String getPreposition()
	{
		return aPreposition;
	}

	
	@Override
	public int hashCode()
	{
		final int lPrime = 31;
		int lResult = 1;
		int lTextHashCode;
		if (aString == null)
		{
			lTextHashCode = 0;
		}
		else
		{
			lTextHashCode = aString.hashCode();
		}
		lResult = lPrime * lResult + lTextHashCode;
		return lResult;
	}

	@Override
	public boolean equals(Object pObject)
	{
		if (this == pObject)
		{
			return true;
		}
		if (pObject == null)
		{
			return false;
		}
		if (getClass() != pObject.getClass())
		{
			return false;
		}
		Task lOther = (Task) pObject;
		if (aString == null)
		{
			if (lOther.aString != null)
			{
				return false;
			}
		}
		else if (!aString.equals(lOther.aString))
		{
			return false;
		}
		return true;
	}
	
}
