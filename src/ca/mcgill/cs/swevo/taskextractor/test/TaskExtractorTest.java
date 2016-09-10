package ca.mcgill.cs.swevo.taskextractor.test;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ca.mcgill.cs.swevo.taskextractor.model.Sentence;
import ca.mcgill.cs.swevo.taskextractor.analysis.TaskExtractor;


public class TaskExtractorTest
{

	@Test
	public final void test()
	{
		TaskExtractor lTaskExtractor = new TaskExtractor();
		List<Sentence> lSentencesWithTasks = lTaskExtractor.extractTasks("This is a test. You can add a <tt>test</tt> to CodeElement. Adding a test.");
		assertEquals(lSentencesWithTasks.get(1).getTasks().get(0).toString().trim(), "add <tt>test</tt> to CodeElement");
		assertEquals(lSentencesWithTasks.get(2).getTasks().get(0).toString().trim(), "add test");

	}

}
