package nlp2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Vector;

class Searcher {
	public static Vector<String> getPosts(String query) { 
			if (query.equals("")) {
				return new Vector<String>();
			}
			query = setTargetLanguage(query);
			//Create a string vector holding all of the URLS we will find.
			Vector<String> urls = new Vector<String>();
			//This key is the unique api key for my permissions.
			String key="AIzaSyCdxjXE_5OXte1mWxPOZ4oSAp7g9p8R-Ac";
	        //This is the input query to do.
			String qry= query;
	        //Convert spaces to http-like-spaces (%20).
			qry = qry.replaceAll(" ", "%20");
	        URL url;
	        try {
	        	//The url is structured to do a custom search which only looks at StackOverflow sites.
				url = new URL("https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=003190347841488233500:p1xqsrhplt4&q="+ qry + "&alt=json" + "&num="+1);
		        
				HttpURLConnection conn;

				//Connect to the URL and set properties of the connection.
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
		        conn.setRequestProperty("Accept", "application/json");
		        
		        //Get the stream of json data from the search response.
		        BufferedReader br;
				br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
				
				//For each line in the response,
		        String output;
				while ((output = br.readLine()) != null) {
					//If we find the format for a link to the search result, get the link's substring and add it to the vector of urls.
					if(output.contains("\"link\": \"")) {                
						String link=output.substring(output.indexOf("\"link\": \"")+("\"link\": \"").length(), output.indexOf("\","));
						urls.addElement(link);       //Will add google search links to vector.
					}
				}			
				//Disconnect for safety.
				conn.disconnect();
			//Handle all types of errors possible without crashing.
	        } catch (ProtocolException e1) {
	        	System.out.println("Couldn't resolve protocol with connection.");
	        	return new Vector<String>();
	        } catch (MalformedURLException e1) {
	        	System.out.println("Couldn't create URL.");
	        	return new Vector<String>();
	        } catch (IOException e) {
				System.out.println("Couldn't open file stream.");
				return new Vector<String>();
			}
	        return urls;
		}
	
	public static String getCodeSnippet(Vector<String> urls) {
		String newText="";
		// Create a new url and open using jsoup so we can do easy queries on the results (formats code for us nicely at cost of time).
        URLReader ur = new URLReader();
        
        // Currently only looks at the first website returned.
        ur.openHtml(urls.elementAt(0));
        if (ur.equals(null)) return newText;
        // Only look at answers that have a code snippet and only look at the first/accepted answer.
        if (!ur.getCode().equals("")) {
        	newText = "# snippet from " + urls.elementAt(0) + " by " + ur.getAuthor() + "\n" + ur.getCode();
        } else {
        	System.out.println("ERROR, could not get code from url: " + urls.elementAt(0));
        }
		return newText;
	}
	
	private static String setTargetLanguage(String text) {
		String language = "java";
		if (text.contains(" in ")) {
        	return text;
        }
		return text + " in " + language;
	}
}