package ca.mcgill.cs.swevo.taskextractor.model;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ca.mcgill.cs.swevo.taskextractor.utils.MapUtils;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

/**
 * Representation of a verbal phrase for task detection. One verbal phrase can map to several tasks.
 * 
 * @author ctreude
 * 
 */
public class VerbalPhrase
{
	private List<List<IndexedWord>> aAccusative;
	private Map<String, List<List<IndexedWord>>> aPrepositions;
	private List<List<IndexedWord>> aVerbs;

	/**
	 * @return short accusative string
	 */
	public String getAccusativeString()
	{
		String lString = "";
		for (List<IndexedWord> lAccusative : aAccusative)
		{
			for (IndexedWord lWord : lAccusative)
			{
				lString += lWord.word().toLowerCase() + " ";
			}
		}
		return lString.trim();
	}

	/**
	 * @return tasks extracted from this verbal phrase
	 */
	public List<Task> getTasks()
	{
		if (aAccusative.isEmpty() && aPrepositions.isEmpty())
		{
			return new ArrayList<Task>();
		}
		List<Task> lTasks = new ArrayList<Task>();
		if (aAccusative.isEmpty())
		{
			aAccusative.add(null);
		}
		if (aPrepositions.isEmpty())
		{
			List<List<IndexedWord>> lEmptyList = new ArrayList<List<IndexedWord>>();
			lEmptyList.add(null);
			aPrepositions.put(null, lEmptyList);
		}

		// a verbal phrase can contain several verbs (e.g., connected by "and"), several direct objects, and several
		// prepositions. To get all tasks from a verbal phrase, the algorithm iterates over all of these and creates a
		// task for each combination.
		for (int lVerbIndex = 0; lVerbIndex < aVerbs.size(); lVerbIndex++)
		{
			List<IndexedWord> lVerb = aVerbs.get(lVerbIndex);
			for (int lAccusativeIndex = 0; lAccusativeIndex < aAccusative.size(); lAccusativeIndex++)
			{
				List<IndexedWord> lAccusative = aAccusative.get(lAccusativeIndex);
				for (Entry<String, List<List<IndexedWord>>> lPrepositionGroup : aPrepositions.entrySet())
				{
					for (int lPrepositionIndex = 0; lPrepositionIndex < lPrepositionGroup.getValue().size(); lPrepositionIndex++)
					{
						List<IndexedWord> lPreposition = aPrepositions.get(lPrepositionGroup.getKey()).get(
								lPrepositionIndex);
						Task lTask = new Task();
						lTask.setVerb(lVerb);
						lTask.setAccusative(lAccusative);
						lTask.setPreposition(lPrepositionGroup.getKey());
						lTask.setPrepositionObject(lPreposition);
						lTasks.add(lTask);
					}
				}
			}
		}
		return lTasks;
	}

	/**
	 * @return short verb string
	 */
	public String getVerbString()
	{
		String lString = "";
		for (List<IndexedWord> lVerb : aVerbs)
		{
			for (IndexedWord lWord : lVerb)
			{
				lString += lWord.lemma().toLowerCase() + " ";
			}
		}
		return lString.trim();
	}

	/**
	 * @param pAccusatives
	 *            accusatives
	 * @param pDependenciesAnnotation
	 *            dependencies
	 */
	public void setAccusatives(List<IndexedWord> pAccusatives, SemanticGraph pDependenciesAnnotation)
	{
		List<GrammaticalRelation> lModifiers = new ArrayList<GrammaticalRelation>();

		// add other nouns and adjectives to the direct object
		lModifiers.add(GrammaticalRelation.valueOf("nn"));
		lModifiers.add(GrammaticalRelation.valueOf("amod"));
		aAccusative = addModifiers(pAccusatives, pDependenciesAnnotation, lModifiers);
	}

	/**
	 * @param pPrepositions
	 *            prepositions
	 * @param pDependencies
	 *            dependencies
	 */
	public void setPrepositions(Map<String, List<IndexedWord>> pPrepositions, SemanticGraph pDependencies)
	{
		List<GrammaticalRelation> lModifiers = new ArrayList<GrammaticalRelation>();

		// add other nouns and adjectives to the prepositional object
		lModifiers.add(GrammaticalRelation.valueOf("nn"));
		lModifiers.add(GrammaticalRelation.valueOf("amod"));
		aPrepositions = addModifiers(pPrepositions, pDependencies, lModifiers);
	}

	/**
	 * @param pVerbs
	 *            verbs
	 * @param pDependenciesAnnotation
	 *            dependencies
	 */
	public void setVerb(List<IndexedWord> pVerbs, SemanticGraph pDependenciesAnnotation)
	{

		List<GrammaticalRelation> lChildModifiers = new ArrayList<GrammaticalRelation>();

		// add negation and particles to verbs
		lChildModifiers.add(GrammaticalRelation.valueOf("neg"));
		lChildModifiers.add(GrammaticalRelation.valueOf("prt"));
		aVerbs = addVerbModifiers(pVerbs, pDependenciesAnnotation, null, lChildModifiers);
	}

	// add additional words that are related to the "main" word of that part of the sentence, in the right order
	private List<IndexedWord> addModifiers(IndexedWord pIndexedWord, SemanticGraph pDependenciesAnnotation,
			List<GrammaticalRelation> pModifiers)
	{
		List<IndexedWord> lIndexedWords = new ArrayList<IndexedWord>();
		Map<IndexedWord, Integer> lTargetValues = new HashMap<IndexedWord, Integer>();
		lTargetValues.put(pIndexedWord, pIndexedWord.index());
		for (IndexedWord lIndexedWord : pDependenciesAnnotation.getChildrenWithRelns(pIndexedWord, pModifiers))
		{
			lTargetValues.put(lIndexedWord, lIndexedWord.index());
		}
		List<Entry<IndexedWord, Integer>> lSortedTargetValues = MapUtils.sortByValueAsc(lTargetValues);
		for (Entry<IndexedWord, Integer> lTargetValue : lSortedTargetValues)
		{
			lIndexedWords.add(lTargetValue.getKey());
		}
		return lIndexedWords;
	}

	private List<List<IndexedWord>> addModifiers(List<IndexedWord> pIndexedWords,
			SemanticGraph pDependenciesAnnotation, List<GrammaticalRelation> pModifiers)
	{
		List<List<IndexedWord>> lIndexedWords = new ArrayList<List<IndexedWord>>();
		for (IndexedWord lIndexedWord : pIndexedWords)
		{
			lIndexedWords.add(addModifiers(lIndexedWord, pDependenciesAnnotation, pModifiers));
		}
		return lIndexedWords;
	}

	private Map<String, List<List<IndexedWord>>> addModifiers(Map<String, List<IndexedWord>> pPrepositions,
			SemanticGraph pDependenciesAnnotation, List<GrammaticalRelation> pModifiers)
	{
		Map<String, List<List<IndexedWord>>> lPrepositions = new HashMap<String, List<List<IndexedWord>>>();
		for (Entry<String, List<IndexedWord>> lPreposition : pPrepositions.entrySet())
		{
			lPrepositions.put(lPreposition.getKey(),
					addModifiers(lPreposition.getValue(), pDependenciesAnnotation, pModifiers));
		}
		return lPrepositions;
	}

	private List<IndexedWord> addVerbModifiers(IndexedWord pIndexedWord, SemanticGraph pDependenciesAnnotation,
			GrammaticalRelation pParentRelation, List<GrammaticalRelation> pChildrenRelations)
	{

		List<IndexedWord> lIndexedWords = new ArrayList<IndexedWord>();
		Map<IndexedWord, Integer> lTargetValues = new HashMap<IndexedWord, Integer>();
		lTargetValues.put(pIndexedWord, pIndexedWord.index());
		for (IndexedWord lIndexedWord : pDependenciesAnnotation.getChildrenWithRelns(pIndexedWord, pChildrenRelations))
		{
			lTargetValues.put(lIndexedWord, lIndexedWord.index());
		}
		if (pParentRelation != null)
		{
			for (IndexedWord lIndexedWord : pDependenciesAnnotation.getParentsWithReln(pIndexedWord, pParentRelation))
			{
				lTargetValues.put(lIndexedWord, lIndexedWord.index());
			}
		}
		List<Entry<IndexedWord, Integer>> lSortedTargetValues = MapUtils.sortByValueAsc(lTargetValues);
		for (Entry<IndexedWord, Integer> lTargetValue : lSortedTargetValues)
		{
			lIndexedWords.add(lTargetValue.getKey());
		}
		return lIndexedWords;
	}

	private List<List<IndexedWord>> addVerbModifiers(List<IndexedWord> pVerbs, SemanticGraph pDependenciesAnnotation,
			GrammaticalRelation pParentRelation, List<GrammaticalRelation> pChildrenRelations)
	{
		List<List<IndexedWord>> lIndexedWords = new ArrayList<List<IndexedWord>>();
		for (IndexedWord lIndexedWord : pVerbs)
		{
			lIndexedWords.add(addVerbModifiers(lIndexedWord, pDependenciesAnnotation, pParentRelation,
					pChildrenRelations));
		}
		return lIndexedWords;
	}

}
