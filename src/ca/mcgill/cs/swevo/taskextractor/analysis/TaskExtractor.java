package ca.mcgill.cs.swevo.taskextractor.analysis;


import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.cs.swevo.taskextractor.model.CodeElementDictionary;
import ca.mcgill.cs.swevo.taskextractor.model.Sentence;
import ca.mcgill.cs.swevo.taskextractor.model.Task;
import ca.mcgill.cs.swevo.taskextractor.utils.NLPUtils;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class deals with the analysis of a text snippet.
 * 
 * @author ctreude
 * 
 */
public class TaskExtractor
{

	private CodeElementAnalyzer aCodeElementAnalyzer;
	private int aCodeElementCount;
	private TaskAnalyzer aTaskAnalyzer;

	/**
	 * constructor.
	 */
	public TaskExtractor()
	{
		aCodeElementCount = 0;
		aTaskAnalyzer = new TaskAnalyzer();
		aCodeElementAnalyzer = new CodeElementAnalyzer();
	}

	/**
	 * This method splits a file into sentences.
	 * 
	 * @param pString
	 *            string to be split
	 * @return list of sentences
	 */
	public List<Sentence> extractTasks(String pString)
	{
		List<Sentence> lSentences = new ArrayList<Sentence>();

		// remove linebreaks
		Scanner lScanner = new Scanner(pString.replace("\n", " "));
		while (lScanner.hasNextLine())
		{
			String lLine = lScanner.nextLine();

			// mask code elements
			lLine = maskCodeElements(lLine);
			List<Sentence> lLineSentences = getSentences(lLine);

			lSentences.addAll(lLineSentences);
		}
		lScanner.close();
		return lSentences;
	}

	// returns sentence maps based on input
	private List<CoreMap> getNLPSentenceMaps(String pInput)
	{
		Annotation lAnnotation = new Annotation(pInput);
		StanfordCoreNLP lPipeline = NLPUtils.getTokenizerPipeline();
		lPipeline.annotate(lAnnotation);
		List<CoreMap> lSentenceMaps = lAnnotation.get(SentencesAnnotation.class);
		return lSentenceMaps;
	}

	// get sentence object based on a sentence
	private Sentence getSentence(String pInput, CoreMap pSentenceMap)
	{
		Sentence lSentence = new Sentence();
		lSentence.setTasks(new ArrayList<Task>());
		String lText = pInput.trim();

		// set sentence text to resolved version of sentence (ce[0-9]+ are replaced by actual code elements)
		Pattern lPattern = Pattern.compile("ce[0-9]+");
		Matcher lMatcher = lPattern.matcher(lText);
		while (lMatcher.find())
		{
			String lUnmasked = CodeElementDictionary.getCodeElementForMask(lMatcher.group());
			if (lUnmasked.startsWith("<tt>"))
			{
				lUnmasked = lUnmasked.replace("<tt>", "");
				lUnmasked = lUnmasked.replace("</tt>", "");
			}
			lText = lText.replace(lMatcher.group(), lUnmasked);
		}
		lSentence.setText(pSentenceMap.get(TextAnnotation.class));

		// determine tasks for sentence
		for (Task lTask : aTaskAnalyzer.extractTasks(pSentenceMap))
		{
			lSentence.addTask(lTask);
		}
		return lSentence;
	}

	private List<Sentence> getSentences(String pInput)
	{
		String lSimplifiedInput = preprocessForNLP(pInput);
		List<CoreMap> lSentenceMaps = getNLPSentenceMaps(lSimplifiedInput);
		List<Sentence> lSentences = new ArrayList<Sentence>();
		for (CoreMap lSentenceMap : lSentenceMaps)
		{
			lSentences.add(getSentence(pInput, lSentenceMap));
		}
		return lSentences;
	}

	// mask code elements as ce[0-9]+
	private String maskCodeElements(String pLine)
	{

		// explicitly marked code elements
		Pattern lExplicitPattern = Pattern.compile("(<tt>)(.*?)(</tt>)");
		Matcher lExplicitMatcher = lExplicitPattern.matcher(pLine);
		String lMaskedLine = pLine;

		// if code element hasn't been detected before, add it to the code element dictionary
		while (lExplicitMatcher.find())
		{
			if (!CodeElementDictionary.containsCodeElement(lExplicitMatcher.group()))
			{
				CodeElementDictionary.putCodeElementsToMask(lExplicitMatcher.group(), "ce" + aCodeElementCount++);
			}
			lMaskedLine = lMaskedLine.replace(lExplicitMatcher.group(),
					CodeElementDictionary.getMaskForCodeElement(lExplicitMatcher.group()));
		}

		// code elements detected through regex
		for (SimpleEntry<Pattern, Integer> lInferredPattern : aCodeElementAnalyzer.getPatterns())
		{
			Matcher lInferredMatcher = lInferredPattern.getKey().matcher(lMaskedLine);
			while (lInferredMatcher.find())
			{
				String lCodeElementValue = lInferredMatcher.group(lInferredPattern.getValue());
				if (!aCodeElementAnalyzer.isCodeElementException(lCodeElementValue))
				{
					Pattern lAlreadyMaskedPattern = Pattern.compile("ce[0-9]+");
					Matcher lAlreadyMaskedMatcher = lAlreadyMaskedPattern.matcher(lCodeElementValue);
					while (lAlreadyMaskedMatcher.find())
					{
						lCodeElementValue = lCodeElementValue.replace(lAlreadyMaskedMatcher.group(),
								CodeElementDictionary.getCodeElementForMask(lAlreadyMaskedMatcher.group()));
					}
					if (!CodeElementDictionary.containsCodeElement(lCodeElementValue))
					{
						CodeElementDictionary.putCodeElementsToMask(lCodeElementValue, "ce" + aCodeElementCount++);
					}
					String lTempString = "";
					lTempString += lMaskedLine.substring(0, lInferredMatcher.start(lInferredPattern.getValue()));
					lTempString += CodeElementDictionary.getMaskForCodeElement(lCodeElementValue);
					lTempString += lMaskedLine.substring(lInferredMatcher.end(lInferredPattern.getValue()));
					lMaskedLine = lTempString;
					lInferredMatcher = lInferredPattern.getKey().matcher(lMaskedLine);
				}
			}
		}
		return lMaskedLine;
	}

	// preprocess text
	private String preprocessForNLP(String pInput)
	{
		String lSimplifiedInput = pInput;
		lSimplifiedInput = lSimplifiedInput.replaceAll("\\(.*?\\)", "");
		lSimplifiedInput = lSimplifiedInput.replaceAll("�", " ");
		lSimplifiedInput = lSimplifiedInput.replaceAll("�", "");
		lSimplifiedInput = lSimplifiedInput.replaceAll("�", "");
		lSimplifiedInput = lSimplifiedInput.replaceAll(" � ", ": ");
		lSimplifiedInput = lSimplifiedInput.trim();

		// if line doesn't end with a period, add period
		if (lSimplifiedInput.matches(".*[a-z0-9]$"))
		{
			lSimplifiedInput += ".";
		}
		return lSimplifiedInput;
	}

}
