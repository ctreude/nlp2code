package ca.mcgill.cs.swevo.taskextractor.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a sentence.
 * 
 * @author ctreude
 * 
 */
public class Sentence
{
	private List<Task> aTasks;
	private String aText;

	/**
	 * @param pTask
	 *            task
	 */
	public void addTask(Task pTask)
	{
		if (aTasks == null)
		{
			aTasks = new ArrayList<Task>();
		}
		aTasks.add(pTask);
	}

	/**
	 * @return tasks
	 */
	public List<Task> getTasks()
	{
		return aTasks;
	}

	/**
	 * @return text
	 */
	public String getText()
	{
		return aText;
	}

	/**
	 * @param pTasks
	 *            tasks
	 */
	public void setTasks(List<Task> pTasks)
	{
		aTasks = pTasks;
	}

	/**
	 * @param pText
	 *            text
	 */
	public void setText(String pText)
	{
		aText = pText;
	}
}
