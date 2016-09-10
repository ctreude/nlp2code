package ca.mcgill.cs.swevo.taskextractor.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * This custom tag annotator for the Stanford NLP parser ensures that all code elements (masked as ce[0-9]+) are tagged
 * as nouns.
 * 
 * @author gayane, ctreude
 * 
 */
public class CustomTagAnnotator implements Annotator
{

	private static final Map<String, String> FIX_POS_TAGS = new HashMap<String, String>()
	{
		private static final long serialVersionUID = 1L;
		{
			put("ce[0-9]+", "NN");
		}
	};

	/**
	 * constructor.
	 */
	public CustomTagAnnotator()
	{
	}

	/**
	 * @param pAnnotatorName
	 *            name of the annotator
	 * @param pProperties
	 *            properties
	 */
	public CustomTagAnnotator(String pAnnotatorName, Properties pProperties)
	{
	}

	private static List<TaggedWord> getPOSTags(List<CoreLabel> pTokens)
	{
		List<List<TaggedWord>> lResult = new ArrayList<List<TaggedWord>>();
		LexicalizedParser lLexicalizedParser = NLPUtils.getLexicalizedParser();
		for (CoreLabel lToken : pTokens)
		{
			String lWord = lToken.get(TextAnnotation.class);
			for (Map.Entry<String, String> lFix : FIX_POS_TAGS.entrySet())
			{
				if (lWord.matches(lFix.getKey()))
				{
					lToken.setTag(lFix.getValue());
				}
			}
		}
		Tree lParse = lLexicalizedParser.apply(pTokens);
		lResult.add(lParse.taggedYield());
		return lResult.get(0);
	}

	@Override
	public void annotate(Annotation pAnnotation)
	{
		for (CoreMap lSentence : pAnnotation.get(CoreAnnotations.SentencesAnnotation.class))
		{
			List<CoreLabel> lTokens = lSentence.get(CoreAnnotations.TokensAnnotation.class);
			List<TaggedWord> lTaggedTokens = getPOSTags(lTokens);
			for (int lCount = 0; lCount < lTokens.size(); ++lCount)
			{
				lTokens.get(lCount).set(PartOfSpeechAnnotation.class, lTaggedTokens.get(lCount).tag());
			}
		}

	}
}
