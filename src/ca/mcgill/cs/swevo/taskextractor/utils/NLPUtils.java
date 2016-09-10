package ca.mcgill.cs.swevo.taskextractor.utils;


import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * Wrapper for the Stanford NLP tooling.
 * 
 * @author ctreude
 * 
 */
public final class NLPUtils
{

	private static LexicalizedParser aLexicalizedParser;
	private static final String PARSER_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private static StanfordCoreNLP tokenizerPipeline = null;

	private NLPUtils()
	{

	}

	/**
	 * @return lexicalized parser
	 */
	public static LexicalizedParser getLexicalizedParser()
	{
		if (aLexicalizedParser == null)
		{
			aLexicalizedParser = LexicalizedParser.loadModel(PARSER_MODEL);
		}
		return aLexicalizedParser;
	}

	/**
	 * @return the NLP processing pipeline for tokenizing only
	 */
	public static StanfordCoreNLP getTokenizerPipeline()
	{
		if (tokenizerPipeline == null)
		{
			Properties lProperties = new Properties();

			// custom tag annotator
			lProperties.put("customAnnotatorClass.clttag", "ca.mcgill.cs.swevo.taskextractor.utils.CustomTagAnnotator");
			lProperties.put("annotators", "tokenize, ssplit, clttag, lemma, parse");
			lProperties.put("tokenize.options", "invertible=true, ptb3Escaping=false, tokenizeNLs=true");

			// disable printing of NLP statements
			PrintStream lDefaultErrorStream = System.err;
			System.setErr(new PrintStream(new OutputStream()
			{
				@Override
				public void write(int pString)
				{
				}
			}));
			tokenizerPipeline = new StanfordCoreNLP(lProperties, false);
			System.setErr(lDefaultErrorStream);
		}
		return tokenizerPipeline;
	}

}
