import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;

public class Main {

	public static void main(String args[]) {
		// Open the file
		try {
			FileInputStream fstream = new FileInputStream("Posts.xml");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			PrintWriter writer_posts = new PrintWriter("sample_posts.txt", "UTF-8");
			Vector<Integer> bools_id = new Vector<Integer>();
			Vector<Integer> bools_bool = new Vector<Integer>();

			String strLine;
			String content;
			String formattedLine;
			String id;

			int counter = 0;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				if (counter == 50)
					break;
				if (strLine.length() < 40)
					continue;
				content = getContent("PostTypeId",strLine);
				if (content.equals("1")) {
					formattedLine = getQuestionData(strLine);
					if (formattedLine.equals("null")) {
						id = getContent("Id",strLine);
						continue;
					} else {
						id = getContent("Id",strLine);

						bools_id.addElement(Integer.valueOf(id));
						bools_bool.addElement(1);
						writer_posts.println(formattedLine);
						counter++;
					}
				} else if (content.equals("2")) {
					formattedLine = getAnswerData(strLine);
					if (formattedLine.equals("null")) {
						continue;
					} else {
						id = getContent("ParentId",strLine);
						if (bools_id.contains(Integer.valueOf(id)) == true) {
							int index = bools_id.indexOf(Integer.parseInt(id));
							if (bools_bool.get(index) == 1) {
								writer_posts.println(formattedLine);
								bools_id.remove(index);
								bools_bool.remove(index);
								counter++;
							} else {
								continue;
							}
						}
					}
				} else {
					continue;
				}				
			}
			//Close the input stream
			br.close();
			writer_posts.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getQuestionData(String post) {
		String formattedLine = "< ";
		String content;
		boolean notag = false;
		boolean notitle = false;

		content = getContent("Id",post);
		formattedLine = formattedLine + "Id=\"" + content + "\" ";

		content = getContent("AcceptedAnswerId",post);
		formattedLine = formattedLine + "AcceptedAnswerId=\"" + content + "\" ";

		String tags = formatResponse(getContent("Tags",post)).replaceAll(";","");
		formattedLine = formattedLine + "Tags=\"" + tags + "\" ";
		
		//If java tag isnt there or if javascript tag is there, return null.
		if (!tags.contains("java") || tags.contains("javascript"))
			notag = true;

		String title = getContent("Title",post);
		formattedLine = formattedLine + "Title=\"" + title + "\" >";

		if (!title.contains("java") || title.contains("javascript"))
			notitle = true;
		if (!(title.contains("how to") || title.contains("How to") || title.contains("how do") || title.contains("How do")))
			notitle = true;
		if (notag == true || notitle == true) 
			return "null";

		int option = 0;
		System.out.println(title);
		System.out.println("Get this post?: ");
		Scanner sc = new Scanner(System.in);
		try {
		    option = Integer.parseInt(sc.nextLine());
		} catch (NumberFormatException e) {
		    e.printStackTrace();
		}
		if (option == 0)
			return "null";

		return formattedLine;
	}

	private static String getAnswerData(String post) {
		String formattedLine = "< ";
		String content;
		content = getContent("Id",post);
		formattedLine = formattedLine + "Id=\"" + content + "\" ";

		content = getContent("ParentId",post);
		formattedLine = formattedLine + "ParentId=\"" + content + "\" ";

		content = formatResponse(getCode(formatResponse(getContent("Body",post))));
		formattedLine = formattedLine + "Code=\"" + content + "\" >";

		if (content.equals("")) {
			return "null";
		}
		
		int count = countOccurances(content,"<newline>");
		if (count > 15) {
			return "null";
		}

		return formattedLine;
	}

	private static String formatResponse(String post) {
		//Fix xml reserved escape chars:
		post = post.replaceAll("&quot", "\"");
		post = post.replaceAll("&apos", "'");
		post = post.replaceAll("&lt", "<");
		post = post.replaceAll("&;lt;","<");
		post = post.replaceAll("&gt", ">");
		post = post.replaceAll("&;gt;",">");
		post = post.replaceAll("&amp", "&");
		//Fix html formatting
		post = post.replaceAll("<;p>;", "");
		post = post.replaceAll("<;/p>;", "");
		post = post.replaceAll("&#xA;", "<newline>");
		post = post.replaceAll("<;pre>;", "");
		post = post.replaceAll("<;/pre>;", "");
		post = post.replaceAll("<;ol>;", "");
		post = post.replaceAll("<;/ol>;", "");
		post = post.replaceAll("<;li>;", "- ");
		post = post.replaceAll("<;/li>;", "");
		post = post.replaceAll("<;br>;", "<newline>");
		//Fix code segments
		post = post.replaceAll("<;code>;", "<code>");
		post = post.replaceAll("<;/code>;", "</code>");
		//Remove block quotes
		post = post.replaceAll("<;blockquote>;", "");
		post = post.replaceAll("<;/blockquote>;", "");

		return post;
	}

	private static String getCode(String post) {
		//Find all occurances of <code> and pull each from that to </code>.
		int start_i = 0;
		int end_i;
		String code = "";
		int counter = 0;
		while ((start_i=post.indexOf("<code>",start_i)) != -1) {
			//length of <code> string.
			start_i += 6;
			if ((end_i = post.indexOf("</code>",start_i)) == -1)
				return code;
			if (counter > 1 && post.substring(start_i,end_i).length() > 10) {
				code = code + "/* code segment " + counter + ": */<newline>";
				code = code + post.substring(start_i,end_i) + "<newline>";
				code = code + "/* --------------- */<newline>";
				counter++;
			} else if (counter == 1 && post.substring(start_i,end_i).length() > 10)  {
				code = "/* code segment " + counter + ": */<newline>" + code + "/* --------------- */<newline>";
				counter++;
				code = code = code + "/* code segment " + counter + ": */<newline>";
				code = code + post.substring(start_i,end_i) + "<newline>";
				code = code + "/* --------------- */<newline>";
				counter++;

			} else {
				code += post.substring(start_i,end_i);
				counter++;
			}
		}
		return code;
	}

	private static String getContent(String tag, String post) {
		int start_i = post.indexOf(tag + "=\"") + tag.length() + 2;
		int end_i = post.indexOf("\"",start_i);
		if (start_i != -1 && end_i != -1) {
			return post.substring(start_i, end_i);
		} else {
			return "null";
		}
	}

	private static int countOccurances(String str, String findstr) {
		int lastIndex = 0;
		int count = 0;
		while(lastIndex != -1){
		    lastIndex = str.indexOf(findstr,lastIndex);
		    if(lastIndex != -1){
		        count ++;
		        lastIndex += findstr.length();
		    }
		}
		return count;
	}
}