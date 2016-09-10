package au.edu.adelaide.cs.nlp2code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.swevo.taskextractor.analysis.TaskExtractor;
import ca.mcgill.cs.swevo.taskextractor.model.Sentence;
import ca.mcgill.cs.swevo.taskextractor.model.Task;

public class AddTasks {

	public static void main(String[] args) throws IOException {
		FileInputStream fis = new FileInputStream("data/howtotitle_tags.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("data/howtotitle_tags_tasks.txt")));
		writer.write("Something");
		TaskExtractor taskExtractor = new TaskExtractor();
		String line;
		while ((line = br.readLine()) != null) {
			String title = line.replace("< Title=\"", "");
			title = title.substring(0, title.indexOf("\""));
			List<Sentence> sentencesWithTasks = taskExtractor
					.extractTasks(title);
			List<String> taskList = new ArrayList<String>();
			for (Sentence sentenceWithTasks : sentencesWithTasks) {
				for (Task task : sentenceWithTasks.getTasks()) {
					taskList.add("<"
							+ task.toString().trim().replaceAll(" +", " ")
									.replaceAll("<", "&lt;")
									.replaceAll(">", "&gt;") + ">");
				}
			}
			writer.write(line.substring(0, line.length() - 1)
					+ "Tasks=\""
					+ taskList.toString().replace("[", "").replace("]", "")
							.replaceAll(">, <", "><") + "\" >\n");
		}
		br.close();
		writer.close();
	}

}
