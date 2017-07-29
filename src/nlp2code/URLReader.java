package nlp2code;

import java.io.IOException;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * class URLReader
 *   Uses JSoup to perform JQuery-like statements on StackOverflow forum pages to retrieve
 *   top code snippets from the StackOverflow page.
 */
public class URLReader {

		// URL to read HTML from.
		private static String address;
		
		/*
		 * Function OpenHTML
		 *   Sets the currently opened address to a new URL.
		 *   
		 *   Input: String address - new URL to read HTML from.
		 */
		public void openHtml(String address) {
			URLReader.address = address;
		}
		
		/*
		 * Function getTopN
		 * 	 Performs JQuery statements on the currently opened URL address to retrieve the top n code snippets
		 *   from the Stack Overflow forum page.
		 *   
		 *   Input: int n - number of code snippets to retreive from the current webpage.
		 *   Returns: Vector<String> - vector of top n code snippets extracted from the Stack Overflow forum page.
		 */
		public Vector<String> getTopN(int n) {
			Vector<String> top_n_snippets = new Vector<String>();
			Document doc;
			String code = "", author;
			try {
				doc = Jsoup.connect(address).get();
				if (doc.equals(null)) return new Vector<String>();
				
				Elements posts = doc.select("div.answer");
				if (!(posts.size() == 0)) { 
					int counter = 0;
					// While there are more answers and we haven't retreived n answers yet.
					while(posts.size() > 0 && counter < n) {
						code = "";
						Element e = posts.first();
						posts.remove(0);
						// Get <pre> tag sections. Code in Stack Overflow posts are surrounded in
						//   <pre><code>  {code here}  </code></pre>
						Elements pres = e.getElementsByTag("pre");
						String code_seg = "";
						
						// Retreive the text in between the two <code> tags, i.e. the code snippet.
						for (int i=0; i<pres.size(); i++) {
							String elem = pres.get(i).toString();
							if (elem.indexOf("</code>") - elem.indexOf("<code>") - 6 < 0) continue;
							code_seg = elem.substring(elem.indexOf("<code>")+6,elem.indexOf("</code>"));
							if (code_seg.equals("")) continue;
							code_seg = formatResponse(code_seg);
							code += code_seg + "\n";
						}
						// Retreive the author of the answer.
						Element user = e.getElementsByClass("user-details").last();
						String text = user.childNode(1).toString();
						text = text.substring(text.indexOf('>')+1);
						text = text.substring(0,text.indexOf('<'));
						author = text;
						
						// Trim whitespace.
						code = code.trim();
						if (code.equals("")) continue;
						
						// Add a reference line to give credit tot he original author.
						top_n_snippets.add("// snippet from " + address + " by " + author + "\n" + code + "\n");
						counter++;
					}
				}
			} catch (IOException e) {
				code = "";
				author = "";
				System.out.println("Could not connect to url using jsoup");
			}	
			return top_n_snippets;
		}
		
		/*
		 * Function formatResponse
		 *   Given a Stack Overflow response post, replace all XML escape character codes with the
		 *   characters they represent.
		 *   
		 *   Input: String post - Stack Overflow answer, or block of text with XML escape character codes.
		 *   Returns: String - formatted post with XML escape character codes removed.
		 */
		private static String formatResponse(String post) {
			//Fix xml reserved escape chars:
			post = post.replaceAll("&;quot;", "\"");
			post = post.replaceAll("&quot;", "\"");
			post = post.replaceAll("&quot", "\"");
			post = post.replaceAll("&;apos;", "'");
			post = post.replaceAll("&apos;", "'");
			post = post.replaceAll("&apos", "'");
			post = post.replaceAll("&;lt;","<");
			post = post.replaceAll("&lt;","<");
			post = post.replaceAll("&lt", "<");
			post = post.replaceAll("&;gt;",">");
			post = post.replaceAll("&gt;", ">");
			post = post.replaceAll("&gt", ">");
			post = post.replaceAll("&;amp;", "&");
			post = post.replaceAll("&amp;", "&");
			post = post.replaceAll("&amp", "&");
			return post;
		}
		
		@Deprecated
		/*
		 * Function getTopAnswer() DEPRECATED
		 *   Retreives the top answer from the current Stack Overflow forum thread.
		 *   
		 *   DEPRECATED - Code may still be useable for other JSoup queries.
		 *   The function getTopN() replaces this.
		 */
		public String getTopAnswer() {
			Document doc;
			String code, author;
			try {
				//Use Jsoup to connect to and get the html data from a http address.
				//Parses it into a form where we can perform Jquery like statements on it.
				doc = Jsoup.connect(address).get();
				if (doc.equals(null)) return "";
				// If we don't have an accepted answer, get the first answer.
				// Only take answers if the number of votes > 10.
				if (!(doc.select("div.accepted-answer").size() == 0)) {
					if (Integer.valueOf(doc.select("div.accepted-answer span.vote-count-post").text()) > 5) {
						code = doc.select("div.accepted-answer pre code").text();
						author = doc.select("div.accepted-answer div.user-details a").last().text();
					} else {
						code = "";
						author = "";
						System.out.println("Error, couldn't get code snippet from: " + address + ", not enough votes for accepted answer");
						return "";
					}
				} else if (!(doc.select("div.answer").size() == 0)) {
					if (Integer.valueOf(doc.select("div.answer span.vote-count-post").first().text()) > 5) {
						code = doc.select("div.answer pre code").first().text();
						author = doc.select("div.answer div.user-details a").last().text();
					} else {
						code = "";
						author = "";
						System.out.println("Error, couldn't get code snippet from: " + address + ", not enough votes for first answer");
						return "";
					}
				} else {
					code = "";
					author = "";
					System.out.println("Error, couldn't get code snippet from: " + address + ", no answers");
					return "";
				}
			} catch (IOException e) {
				code = "";
				author = "";
				System.out.println("Could not connect to url using jsoup");
			}
			return "// snippet from " + address + " by " + author + "\n" + code;
		}
}