package nlp2code;

import java.io.IOException;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//TODO when selecting code, get the biggest code snippet from each response, not the whole thing just .text()
public class URLReader {

		private static String address;
		
		public void openHtml(String address) {
			URLReader.address = address;
		}
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
					while(posts.size() > 0 && counter < n) {
						code = "";
						Element e = posts.first();
						posts.remove(0);
						Elements pres = e.getElementsByTag("pre");
						String code_seg = "";
						for (int i=0; i<pres.size(); i++) {
							String elem = pres.get(i).toString();
							if (elem.indexOf("</code>") - elem.indexOf("<code>") - 6 < 0) continue;
							code_seg = elem.substring(elem.indexOf("<code>")+6,elem.indexOf("</code>"));
							if (code_seg.equals("")) continue;
							code_seg = formatResponse(code_seg);
							code += code_seg + "\n";
						}
						Element user = e.getElementsByClass("user-details").last();
						String text = user.childNode(1).toString();
						text = text.substring(text.indexOf('>')+1);
						text = text.substring(0,text.indexOf('<'));
						author = text;
						code = code.trim();
						if (code.equals("")) continue;
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