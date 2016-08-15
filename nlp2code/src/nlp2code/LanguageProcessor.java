package nlp2code;

import java.util.Vector;

// Language processor class to hold all functions to process a query.
public class LanguageProcessor {

		// Function public Vector<String> process(String)
		// purpose: to handle an input string as a query and convert that to tags to query Stack Overflow posts.
		//			removes punctuation, changes to lower case, lemmatizes, tokenises and removes stop words from input string.
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
				
				// If the string is not empty, add it to the list of tags.
				if (str != "")
					tags.add(str);
			}
			
			// return the vector of tags.
			return tags;
		}
		
}
