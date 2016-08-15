package nlp2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

// Language processor class to hold all functions to process a query.
public class LanguageProcessor {

		private Vector<String> stopwords;
		
		public LanguageProcessor() {
			stopwords = getStopWords();
		}
		
		// Function public Vector<String> process(String)
		// purpose: to handle an input string as a query and convert that to tags to query Stack Overflow posts.
		//			removes punctuation, changes to lower case, stems, tokenises and removes stop words from input string.
		public Vector<String> process(String input) {
			Vector<String> tags = new Vector<String>();
			
			// To remove punctuation only: replaceAll("\\p{P}", "")
			// Changes text to lower case and removes all non-letter characters. Splits by whitespace.
			// Code from: http://stackoverflow.com/questions/18830813/how-can-i-remove-punctuation-from-input-text-in-java
			String[] words = input.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
			
			
			String str;
			Stemmer pstem = new Stemmer();
			// For each word in the input.
			for (int i=0; i<words.length; i++) {
				str = words[i];
				
				pstem.add(str.toCharArray(),str.length());
				pstem.stem();
				str = pstem.toString();
				pstem.resetStemmer();
				
				// If the word is not a stop word and is not empty, add it to the list of tags.
				if (!stopwords.contains(str) && str != "")
					tags.add(str);
			}
			
			// return the vector of tags.
			return tags;
		}
		
		/**
		 * function to open stopwords.txt file to load in all common words to remove from a piece of text.
		 * returns a vector of strings containing each stop word in stopwords.txt
		 * words in stopwords.txt must be comma-separated or line-separated.
		 * 
		 * main code skeleton pulled from:
		 *  http://blog.vogella.com/2010/07/06/reading-resources-from-plugin/
		 */
		private Vector<String> getStopWords() {
			Vector<String> vect = new Vector<String>();
			
			URL url;
			try {
			        url = new URL("platform:/plugin/nlp2code/data/stopwords.txt");
			    InputStream inputStream = url.openConnection().getInputStream();
			    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			    String inputLine;
			 
			    while ((inputLine = in.readLine()) != null) {
			    	String[] words = inputLine.split(",");
					for (int i=0; i<words.length; i++) {
						words[i] = words[i].replaceAll("\\s+", "");
						vect.add(words[i]);
					}
			    }
			 
			    in.close();
			 
			} catch (IOException e) {
			    e.printStackTrace();
			}
			
			return vect;
		}
		
}
