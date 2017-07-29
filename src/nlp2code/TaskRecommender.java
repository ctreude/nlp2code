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

/** 
 * Class TaskRecommender
 *  Implements the interface that controls code recommenders.
 *  Provides all functionality required to load and recommend relevant tasks for an incomplete query.
 */
public class TaskRecommender implements IJavaCompletionProposalComputer{
	
	// Stores tasks from the task database file.
	static HashMap<String,String> queries_map = new HashMap<String,String>();
	// Stores the list of recommendation tasks/queries for each invocation of the content assist tool.
	static ArrayList<String> queries = new ArrayList<String>();
	// Ddefines whether we are querying via a task or not.
	static boolean query_task = true;
	// Defines which types of content assist to load (Developer use only).
	private boolean task_auto_completes = true;
	private boolean title_auto_completes = false;
	// Defines the maximum number of recommendations to include in the content assist window (less recommendations for greater performance).
	private int MAX_NUM_RECOMMENDATIONS = 100;
	
	/* 
	 * Function computeCompletionProposals
	 *  Implements an interface to compile a list of proposed autocompletions for a given incomplete query.
	 *  Each proposal is defined as a string proposal to insert into a specific section of the current editor.
	 */
	// Override the computeCompletionProposals to return a list of proposals for the content assist window to display from this plugin.
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext arg0, IProgressMonitor arg1) {		
		// If we haven't used the plugin yet, load tasks into memory for faster searches.
		if (!(queries_map instanceof HashMap<?,?> && queries instanceof ArrayList<?>)) {
			queries_map = new HashMap<String,String>();
			queries = new ArrayList<String>();
			// Chooses which type of autocompletes to load (see class variables).
			if (task_auto_completes)
				loadTasks();
			if (title_auto_completes)
				loadTitles();
			// If neither auto-completes are selected to load, load the task auto completes.
			if (!task_auto_completes && !title_auto_completes)
				loadTasks();
		}
		
		// Add plugin query document listener to current editor if not already present.
		addListenerToCurrentEditor();
		
		// Pre-define some variables
		String line = "";
		int line_num = 0;
		IDocument doc;
		
		// Get the active editor.
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		// This code block gets the current selection in the active editor.
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
		
		// If the last character of the selection is a end-of-line character, make sure we don't insert end-of-line characters in the content assist.
		boolean eol = false;
		if (line.endsWith("\n")) eol = true;
		
		// Remove leading and trailing whitespace for the partial query (save the leading whitespace for later).
		String whitespace_before;
		if (line.indexOf(line.trim()) < 0) {
			whitespace_before = line;
		} else {
			whitespace_before = line.substring(0, line.indexOf(line.trim()));
		}
		
		// Calculate offset of partial query. Used when inserting a recommendation to ensure the query is inserted correctly.
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
		
		// Get the line length and line offset to ensure we replace the correct location with any completion.
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
			line_offset + extra_offset, // replacement offset
			line_length - extra_offset, // replace the full text
			searchResult.length())); // length of string to replace
		}
		return proposals;
	}
	
	/* 
	 * Function addListenerToCurrentEditor
	 *  Adds the document listener to detect ?{query}? type searches in the case that the current editor
	 *  doesn't already have the listener.
	 */
	private void addListenerToCurrentEditor() {
		// If we don't have the document listener currently active (for detecting ?{query}? queries) for the current document, add it.
		ITextEditor current_editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument current_doc = current_editor.getDocumentProvider().getDocument(current_editor.getEditorInput());
		if (!InputHandler.documents.contains(current_doc)) {
			current_doc.addDocumentListener(InputHandler.qdl);
		}
	}

	/* 
	 * Function findQueries
	 *  Given a string to search for, return all strings in static hashmap queries that contain the search string.
	 */
	private List<String> findQueries(String search) {
		List<String> result = new ArrayList<String>();
		for (String query : queries) {
			if (query.contains(search) || search == "") {
				// If we reach the maximum number of queries to recommend, stop.
				if (result.size() == MAX_NUM_RECOMMENDATIONS)
					return result;
				result.add(query);
			}
		}
		return result;
	}
	
	/*
	 * Function loadTasks
	 *  Loads the Extracted task to Stack Overflow question ID mapping database into a map.
	 *  Purpose: Loads Task->Id mapping into queries_map.
	 */
	public static void loadTasks() {
		URL url;
		try {
			// Using this url assumes nlp2code exists in the 'plugin' folder for eclipse.
			// This is true when testing the plugin (in a temporary platform) and after installing the plugin.
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
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	/*
	 * Function loadTitles
	 *  Loads the Stack Overflow title to question ID mapping database into a map.
	 *  Purpose: Loads Question Title->Id mapping into queries_map.
	 */
	private void loadTitles() {
		URL url;
		try {
			// Using this url assumes nlp2code exists in the 'plugin' folder for eclipse.
			// This is true when testing the plugin (in a temporary platform) and after installing the plugin.
		    url = new URL("platform:/plugin/nlp2code/data/title,id.txt");
		    InputStream inputStream = url.openConnection().getInputStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		    String inputLine;
		 
		    while ((inputLine = in.readLine()) != null) {
		    	String query = inputLine.substring(0, inputLine.indexOf(","));
		    	String id = inputLine.substring(inputLine.indexOf(",")+1);
			    queries_map.put(query, id);
		    }
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
		
	// Function implementations required for the interface that are not used.
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext arg0, IProgressMonitor arg1) { return null; }
	@Override
	public String getErrorMessage() { return null; }
	@Override
	public void sessionEnded() { }
	@Override
	public void sessionStarted() { }
}
