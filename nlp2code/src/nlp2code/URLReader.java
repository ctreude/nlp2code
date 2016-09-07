package nlp2code;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class URLReader {

		private static String code;
		private static String author;
		
		public void openHtml(String address) {
			
			Document doc;
			try {
				//Use Jsoup to connect to and get the html data from a http address.
				//Parses it into a form where we can perform Jquery like statements on it.
				doc = Jsoup.connect(address).get();
				if (doc.equals(null)) return;
				// If we don't have an accepted answer, do nothing.
				if (doc.select("div.accepted-answer").size() == 0) {
					code = "";
					author = "";
					return;
				}
				//Get the accepted answer code and name using jsoup's jquery interface.
				code = doc.select("div.accepted-answer pre code").text();
				author = doc.select("div.accepted-answer div.user-details a").last().text();
			} catch (IOException e) {
				System.out.println("Could not connect to url using jsoup");
			}
		}
		
		public String getCode() { return code; }
		public String getAuthor() { return author; }
		
}
