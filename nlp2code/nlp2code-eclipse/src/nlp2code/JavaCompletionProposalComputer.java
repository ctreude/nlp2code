package nlp2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class JavaCompletionProposalComputer implements IJavaCompletionProposalComputer{
	
	// Temporary way of holding all possible tasks.
	static HashMap<String,String> queries_map = new HashMap<String,String>();
	static ArrayList<String> queries = new ArrayList<String>();
	//static HashMap<String,String> temp_map;
	static boolean query_task = true;
	private boolean task_auto_completes = true;
	private boolean title_auto_completes = false;
	
	private int MAX_NUM_RECOMMENDATIONS = 100;
	
	// Override the computeCompletionProposals to return a list of proposals for the content assist window to display from this plugin.
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext arg0, IProgressMonitor arg1) {
		if (!(queries_map instanceof HashMap<?,?> && queries instanceof ArrayList<?>)) {
			queries_map = new HashMap<String,String>();
			queries = new ArrayList<String>();
			//temp_map = new HashMap<String,String>();
			if (task_auto_completes)
				loadTasks();
			if (title_auto_completes)
				loadTitles();
			//if neither auto-completes selected, load the tasks one.
			if (!task_auto_completes && !title_auto_completes)
				loadTasks();
		}
		
		ITextEditor current_editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument current_doc = current_editor.getDocumentProvider().getDocument(current_editor.getEditorInput());
		if (!InputHandler.documents.contains(current_doc)) {
			current_doc.addDocumentListener(InputHandler.qdl);
		}
		
		// Pre-define some variables
		String line = "";
		int line_num = 0;
		IDocument doc;
		
		// Get the active editor.
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		// If we are dealing with a text editor. 
		if(editor instanceof ITextEditor) {
			// Get the current selection of the document (for determining what line a query is on).
		    ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
		    if (selectionProvider.equals(null)) return new ArrayList<ICompletionProposal>();
		    ISelection selection = selectionProvider.getSelection();
		    if (selection.equals(null)) return new ArrayList<ICompletionProposal>();
		    ITextEditor ite = (ITextEditor)editor;
		    if (ite.equals(null)) return new ArrayList<ICompletionProposal>();
		    // Get the current document (for isolating substring of text in document using line number from selection).
		    doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
		    if (doc.equals(null)) return new ArrayList<ICompletionProposal>();
		    if (selection instanceof ITextSelection) {
		        ITextSelection textSelection = (ITextSelection)selection;
		        try {
		        	// Get the line number we are currently on.
		        	line_num = textSelection.getStartLine();
		        	// Get the string on the current line and use that as the query line to be auto-completed.
					line = doc.get(doc.getLineOffset(doc.getLineOfOffset(textSelection.getOffset())), doc.getLineLength(doc.getLineOfOffset(textSelection.getOffset())));
				} catch (BadLocationException e) {
					System.out.println("Error with getting input query.");
					e.printStackTrace();
					return null;
				}
		    }
		} else {
			// If we are not dealing with a text editor.
			return null;
		}
		boolean eol = false;
		if (line.endsWith("\n")) eol = true;
		
		// Remove leading and trailing whitespace.
		String whitespace_before;
		if (line.indexOf(line.trim()) < 0) {
			whitespace_before = line;
		} else {
			whitespace_before = line.substring(0, line.indexOf(line.trim()));
		}
		int extra_offset = whitespace_before.length();
		line = line.trim();
		if (line.startsWith("?",0)) {
			if (line.length() > 1)
				line=line.substring(1, line.length());
			else
				line = "";
		}
		line = line.toLowerCase();
		// Gather proposals
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		
		int line_length = 0;
		int line_offset = 0;
		
		try {
			line_length = doc.getLineLength(line_num);
			line_offset = doc.getLineOffset(line_num);
			if (eol) line_length-=1;
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		// For each query that matches the line, create a new proposal and add it to the proposals arraylist. 
		for (String searchResult : findQueries(line)) {
			proposals.add(new CompletionProposal(
			searchResult.substring(0, 1).toUpperCase() + searchResult.substring(1) + "?", // replacement text, first letter is capital
			line_offset + extra_offset, // replacement offset with
			line_length - extra_offset, // replace the full text
			searchResult.length())); // length of string to replace
		}
		return proposals;
	}
	
	// Function which when given an input substring search, will return a list of all elements in queries which contain the substring.
	private List<String> findQueries(String search) {
		List<String> result = new ArrayList<String>();
		for (String query : queries) {
			if (query.contains(search) || search == "") {
				if (result.size() == MAX_NUM_RECOMMENDATIONS)
					return result;
				result.add(query);
			}
		}
		return result;
	}
	
	
	public static void loadTasks() {
		URL url;
		try {
		    url = new URL("platform:/plugin/nlp2code/data/task,id.txt");
		    InputStream inputStream = url.openConnection().getInputStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		    String inputLine;
		 
		    while ((inputLine = in.readLine()) != null) {
		    	String task = inputLine.substring(0, inputLine.indexOf(","));
		    	String ids = inputLine.substring(inputLine.indexOf(",")+1);
			    queries_map.put(task, ids);
			    queries.add(task);
		    }
		    //close the file stream.
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	
	private void loadTitles() {
		URL url;
		try {
		    url = new URL("platform:/plugin/nlp2code/data/title,id.txt");
		    InputStream inputStream = url.openConnection().getInputStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		    String inputLine;
		 
		    while ((inputLine = in.readLine()) != null) {
		    	String query = inputLine.substring(0, inputLine.indexOf(","));
		    	String id = inputLine.substring(inputLine.indexOf(",")+1);
			    queries_map.put(query, id);
		    }
		    //close the file stream.
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	/*
	private void titleToId() {
		try {
			PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
			Iterator it = queries_map.entrySet().iterator();
		    while (it.hasNext()) {
		        HashMap.Entry pair = (HashMap.Entry)it.next();
		        String task = pair.getKey().toString();
		        String[] titles = pair.getValue().toString().split(",");
		        
		        String ids = "";
		        for (int i=0; i<titles.length; i++) {
		        	
		        	if (temp_map.containsKey(titles[i]) && !temp_map.get(titles[i]).equals("") && !temp_map.get(titles[i]).equals(null))
		        		ids += temp_map.get(titles[i]) + ",";
		        	else
		        		continue;
		        }
		        if (ids.equals(""))
		        	continue;
		        ids = ids.substring(0,ids.length()-1);
		        writer.println(pair.getKey().toString() + "," + ids);
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
	/*
	private void loadTasks() {
		//New vector to hold stopwords.		
		URL url;
		try {
			//Open up the stopwords.txt file in the plugin native files.
		    url = new URL("platform:/plugin/nlp2code/data/task,title.txt");
		    InputStream inputStream = url.openConnection().getInputStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		    String inputLine;
		 
		    //Read the stopwords line by line (comma-separated or newline separated) and put them into the stopwords vector.
		    while ((inputLine = in.readLine()) != null) {
		    	String[] words = inputLine.split(",");
		    	if (words.length > 1) {
		    		words[0] = checkTaskFormatting(words[0]);
		    		words[1] = checkTaskFormatting(words[1]);
		    		if (words[1].equals("")) continue;
		    		if (words[0].equals("")) continue;
		    		if (queries_map.containsKey(words[0])) {
		    			queries_map.replace(words[0], queries_map.get(words[0]) + "," + words[1]);
		    		} else {
			    		queries_map.put(words[0], words[1]);
			    		queries.add(words[0]);
		    		}
		    	}
		    }
		    //close the file stream.
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}	
	
	
	private void saveTasks() {
		try {
			PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
			Iterator it = queries_map.entrySet().iterator();
		    while (it.hasNext()) {
		        HashMap.Entry pair = (HashMap.Entry)it.next();
		        if (pair.getKey().toString().length() > 1)
		        	writer.println(pair.getKey() + "," + pair.getValue());
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String checkTaskFormatting(String task) {
		//Fix xml reserved escape chars:
		task = task.replaceAll("&quot", "\"");
		task = task.replaceAll("&apos", "'");
		task = task.replaceAll("&lt", "<");
		task = task.replaceAll("&;lt;","<");
		task = task.replaceAll("&gt", ">");
		task = task.replaceAll("&;gt;",">");
		task = task.replaceAll("&amp", "&");
		task = task.toLowerCase();
		if (task.matches("[abcdefghijklmnopqrstuvwxyz\"><'& ]*")) {
			return task;
		} else {
			return "";
		}
	}
	*/
	
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext arg0, IProgressMonitor arg1) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
	
	@Override
	public void sessionEnded() {
	}

	@Override
	public void sessionStarted() {
	}
}
