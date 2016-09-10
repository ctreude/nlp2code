package ca.mcgill.cs.swevo.taskextractor.analysis;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.mcgill.cs.swevo.taskextractor.model.Task;
import ca.mcgill.cs.swevo.taskextractor.model.VerbalPhrase;
import ca.mcgill.cs.swevo.taskextractor.utils.Configuration;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class extracts tasks from sentences.
 * 
 * @author ctreude
 * 
 */
public class TaskAnalyzer
{

	/**
	 * This method extracts a list of tasks from a sentence given the NLP sentence map.
	 * 
	 * @param pSentenceMap
	 *            sentence map
	 * @return list of tasks
	 */
	public List<Task> extractTasks(CoreMap pSentenceMap)
	{
		// get dependencies
		SemanticGraph lCollapsedDependenciesAnnotation = pSentenceMap
				.get(CollapsedCCProcessedDependenciesAnnotation.class);

		// find all dobj, prep, agent, nsubjpass, and rcmod dependencies
		List<SemanticGraphEdge> lRelations = lCollapsedDependenciesAnnotation.findAllRelns(GrammaticalRelation
				.valueOf("dobj"));
		Collection<GrammaticalRelation> lPrepositionRelations = new ArrayList<GrammaticalRelation>();
		for (TypedDependency lTypedDependency : lCollapsedDependenciesAnnotation.typedDependencies())
		{
			GrammaticalRelation lGrammaticalRelation = lTypedDependency.reln();
			if (lGrammaticalRelation.toString().startsWith("prep") || lGrammaticalRelation.toString().equals("agent"))
			{
				lPrepositionRelations.add(lGrammaticalRelation);
				lRelations.addAll(lCollapsedDependenciesAnnotation.findAllRelns(lGrammaticalRelation));
			}
		}
		lRelations.addAll(lCollapsedDependenciesAnnotation.findAllRelns(GrammaticalRelation.valueOf("nsubjpass")));

		List<SemanticGraphEdge> lRcModRelations = lCollapsedDependenciesAnnotation.findAllRelns(GrammaticalRelation
				.valueOf("rcmod"));
		for (SemanticGraphEdge lRcModRelation : lRcModRelations)
		{
			lRelations.add(new SemanticGraphEdge(lRcModRelation.getTarget(), null, null, 0));
		}

		return getTasks(lCollapsedDependenciesAnnotation, lRelations, lPrepositionRelations);
	}

	// get prepositions for a given verb and list of accusatives
	private Map<String, List<IndexedWord>> getPrepositions(SemanticGraph pCollapsedDependenciesAnnotation,
			Collection<GrammaticalRelation> pPrepositionRelations, IndexedWord pVerb, List<IndexedWord> pAccusatives)
	{
		Map<String, List<IndexedWord>> lAllPreps = new HashMap<String, List<IndexedWord>>();
		for (GrammaticalRelation lPrepositionRelation : pPrepositionRelations)
		{
			String lPreposition = lPrepositionRelation.getSpecific();
			if (lPreposition == null)
			{
				continue;
			}
			if (lPrepositionRelation.toString().equals("agent"))
			{
				lPreposition = "by";
			}

			// add prepositions to verbs
			List<IndexedWord> lPreps = pCollapsedDependenciesAnnotation
					.getChildrenWithReln(pVerb, lPrepositionRelation);

			// add prepositions to accusatives
			for (IndexedWord lAccusative : pAccusatives)
			{
				lPreps.addAll(pCollapsedDependenciesAnnotation.getChildrenWithReln(lAccusative, lPrepositionRelation));
			}
			lAllPreps.put(lPreposition, new ArrayList<IndexedWord>());
			for (IndexedWord lPrep : lPreps)
			{

				// filter out empty and generic accusatives, and accusatives that aren't nouns or VBGs
				if (!lPrep.word().trim().equals("")
						&& !Configuration.getInstance().getGenericAccusatives().contains(lPrep.word())
						&& (lPrep.tag().startsWith("NN") || lPrep.tag().startsWith("VBG")))
				{
					lAllPreps.get(lPreposition).add(lPrep);
				}
			}
			if (lAllPreps.get(lPreposition).isEmpty())
			{
				lAllPreps.remove(lPreposition);
			}
		}
		return lAllPreps;
	}

	private List<Task> getTasks(SemanticGraph pCollapsedDependenciesAnnotation, List<SemanticGraphEdge> pRelations,
			Collection<GrammaticalRelation> pPrepositionRelations)
	{
		List<IndexedWord> lRelationSources = new ArrayList<IndexedWord>();
		for (SemanticGraphEdge lRelation : pRelations)
		{
			lRelationSources.add(lRelation.getSource());
		}
		return getTasksFromDependencySources(pCollapsedDependenciesAnnotation, pPrepositionRelations, lRelationSources);
	}

	// get verb and object part of tasks
	private List<Task> getTasksFromDependencySources(SemanticGraph pCollapsedDependenciesAnnotation,
			Collection<GrammaticalRelation> pPrepositionRelations, List<IndexedWord> pRelationSources)
	{
		List<Task> lTasks = new ArrayList<Task>();
		for (IndexedWord lVerb : pRelationSources)
		{
			VerbalPhrase lVerbalPhrase = new VerbalPhrase();

			// filter out verbs that aren't VB or aren't words
			if (!lVerb.tag().startsWith("VB") || !lVerb.word().toLowerCase().matches("[a-z ]+"))
			{
				continue;
			}
			List<IndexedWord> lVerbs = new ArrayList<IndexedWord>();
			lVerbs.add(lVerb);
			lVerbalPhrase.setVerb(lVerbs, pCollapsedDependenciesAnnotation);

			// skip verbs that aren't programming actions
			if (!Configuration.getInstance().getProgrammingActions().contains(lVerbalPhrase.getVerbString()))
			{
				continue;
			}

			// add tasks for each "and" between verbs
			for (IndexedWord lConjVerb : pCollapsedDependenciesAnnotation.getChildrenWithReln(lVerb,
					GrammaticalRelation.valueOf("conj_and")))
			{
				if (lConjVerb.tag().startsWith("VB")
						&& lConjVerb.word().toLowerCase().matches("[a-z ]+")
						&& Configuration.getInstance().getProgrammingActions()
								.contains(lConjVerb.lemma().toLowerCase()) && !pRelationSources.contains(lConjVerb))
				{
					lVerbs.add(lConjVerb);
				}
			}

			// add tasks for each "or" between verbs
			for (IndexedWord lConjVerb : pCollapsedDependenciesAnnotation.getChildrenWithReln(lVerb,
					GrammaticalRelation.valueOf("conj_or")))
			{
				if (lConjVerb.tag().startsWith("VB")
						&& lConjVerb.word().toLowerCase().matches("[a-z ]+")
						&& Configuration.getInstance().getProgrammingActions()
								.contains(lConjVerb.lemma().toLowerCase()) && !pRelationSources.contains(lConjVerb))
				{
					lVerbs.add(lConjVerb);
				}
			}
			lVerbalPhrase.setVerb(lVerbs, pCollapsedDependenciesAnnotation);

			// get accusatives
			List<IndexedWord> lAllAccusatives = new ArrayList<IndexedWord>();
			List<IndexedWord> lAccusatives = pCollapsedDependenciesAnnotation.getChildrenWithReln(lVerb,
					GrammaticalRelation.valueOf("dobj"));
			lAccusatives.addAll(pCollapsedDependenciesAnnotation.getChildrenWithReln(lVerb,
					GrammaticalRelation.valueOf("nsubjpass")));

			lAccusatives.addAll(pCollapsedDependenciesAnnotation.getParentsWithReln(lVerb,
					GrammaticalRelation.valueOf("rcmod")));
			lAllAccusatives.addAll(lAccusatives);
			List<IndexedWord> lFinalAccusatives = getFinalAccusatives(pCollapsedDependenciesAnnotation, lVerbalPhrase,
					lAllAccusatives);
			lVerbalPhrase.setAccusatives(lFinalAccusatives, pCollapsedDependenciesAnnotation);
			Map<String, List<IndexedWord>> lAllPreps = getPrepositions(pCollapsedDependenciesAnnotation,
					pPrepositionRelations, lVerb, lFinalAccusatives);
			lVerbalPhrase.setPrepositions(lAllPreps, pCollapsedDependenciesAnnotation);
			for (Task lTask : lVerbalPhrase.getTasks())
			{
				if (!lTask.containedIn(lTasks))
				{
					lTasks.add(lTask);
				}
			}
		}
		return lTasks;
	}

	private List<IndexedWord> getFinalAccusatives(SemanticGraph pCollapsedDependenciesAnnotation,
			VerbalPhrase pVerbalPhrase, List<IndexedWord> pAllAccusatives)
	{
		List<IndexedWord> lFinalAccusatives = new ArrayList<IndexedWord>();
		for (IndexedWord lAccusative : pAllAccusatives)
		{

			// filter out accusatives that aren't nouns
			if (!lAccusative.tag().startsWith("NN"))
			{
				continue;
			}
			List<IndexedWord> lAccusativeList = new ArrayList<IndexedWord>();
			lAccusativeList.add(lAccusative);
			pVerbalPhrase.setAccusatives(lAccusativeList, pCollapsedDependenciesAnnotation);
			if (!Configuration.getInstance().getGenericAccusatives().contains(pVerbalPhrase.getAccusativeString()))
			{
				// add tasks for each "and" between direct objects
				lFinalAccusatives.add(lAccusative);
				for (IndexedWord lConjAcc : pCollapsedDependenciesAnnotation.getChildrenWithReln(lAccusative,
						GrammaticalRelation.valueOf("conj_and")))
				{
					List<IndexedWord> lConjAccList = new ArrayList<IndexedWord>();
					lConjAccList.add(lConjAcc);
					pVerbalPhrase.setAccusatives(lConjAccList, pCollapsedDependenciesAnnotation);
					if (!Configuration.getInstance().getGenericAccusatives()
							.contains(pVerbalPhrase.getAccusativeString())
							&& lConjAcc.tag().startsWith("NN"))
					{
						lFinalAccusatives.add(lConjAcc);
					}
				}

				// add tasks for each "or" between direct objects
				for (IndexedWord lConjAcc : pCollapsedDependenciesAnnotation.getChildrenWithReln(lAccusative,
						GrammaticalRelation.valueOf("conj_or")))
				{
					List<IndexedWord> lConjAccList = new ArrayList<IndexedWord>();
					lConjAccList.add(lConjAcc);
					pVerbalPhrase.setAccusatives(lConjAccList, pCollapsedDependenciesAnnotation);
					if (!Configuration.getInstance().getGenericAccusatives()
							.contains(pVerbalPhrase.getAccusativeString())
							&& lConjAcc.tag().startsWith("NN"))
					{
						lFinalAccusatives.add(lConjAcc);
					}
				}
			}
		}
		return lFinalAccusatives;
	}

}
