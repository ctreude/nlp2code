package nlp2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * class InputHandler
 *  Implements the required functionality to search for a query via the search button in the eclipse toolbar.
 */
public class InputHandler extends AbstractHandler {
	// Holds history of previous code snippets (from previous queries) to enable undo functionality.
	static Vector<String> previous_search = new Vector<String>();
	// Holds the previous query (equivilent to previous_search[last]).
	static String previous_query = "";
	// Offset of the previous query (to re-insert when using undo).
	static int previous_offset = 0;
	// Length of the previous query (to re-insert when using undo).
	static int previous_length = 0;
	// Holds previous queries.
	static Vector<String> previous_queries = new Vector<String>();
	// Listens for when cycling finishes and prompts for feedback afterwards.
	static CycleDocListener doclistener = new CycleDocListener();
	// Create a listener to handle searches via the editor in ?{querey}? format.
	static QueryDocListener qdl = new QueryDocListener();
	// A vector containing all documents that have an active query document listener.
	static Vector<IDocument> documents = new Vector<IDocument>();
	
	/*
	 * Function execute
	 *  Runs when the Stack Overflow Search button is pressed.
	 *  Checks the current selection and/or line of text for a valid query.
	 *  Valid queries are searched
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ITextEditor current_editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument current_doc = current_editor.getDocumentProvider().getDocument(current_editor.getEditorInput());
		if (!InputHandler.documents.contains(current_doc)) {
			current_doc.addDocumentListener(InputHandler.qdl);
		}

		// Get the current active editor.
	    IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	    
	    // If the current active editor is a text editor (i.e. not any other workbench component)
	    if ( part instanceof ITextEditor ) {
	    	// Use text editor context to locate the document of the editor and get the input stream to that editor.
	        ITextEditor editor = (ITextEditor)part;
	        IDocumentProvider prov = editor.getDocumentProvider();
	        IDocument doc = prov.getDocument( editor.getEditorInput() );
	        
	        // Get the current selection to query on. 
	        ISelection sel = editor.getSelectionProvider().getSelection();
	        
	        if (sel instanceof ITextSelection) {
		        ITextSelection textSelection = (ITextSelection)sel;
		        try {
		        	// Get the line number we are currently on.
		        	if (textSelection.equals(null)) return null;
		        	int offset = textSelection.getOffset();
		        	// Get the string on the current line and use that as the query line to be auto-completed.
		        	if (offset > doc.getLength() || offset < 0) return null;
					String text = doc.get(doc.getLineOffset(doc.getLineOfOffset(offset)), doc.getLineLength(doc.getLineOfOffset(offset)));
					if (text.equals("") || text.equals(null)) return null;
					boolean eol = false;
					if (text.endsWith("\n")) eol = true;
					String whitespace_before = text.substring(0, text.indexOf(text.trim()));
		            text = text.trim();
		            
		            // Get the vector of URLS from the getPosts query.		            
		            Vector<String> results = Searcher.getThreads(text);
		            if (results.size() == 0) return null;
		            Vector<String> url = new Vector<String>();
		            for (int i=0; i<results.size(); i++) {
        		    	url.add(results.get(i));
        		    }
		            
		            // Get the accepted answer code snippet.
		            Vector<String> code = Searcher.getCodeSnippets(url);
		            if (code.size() == 0) return null;
		            Vector<String> fixed_code = fixSpacing(code,whitespace_before);
        		    previous_search.clear();
        		    previous_search = fixed_code;
        		    previous_query = text;
        		    
        		    // Find line length and offset to replace.
        		    int line_num = doc.getLineOfOffset(offset);
        		    int line_length = 0;
        			int line_offset = 0;
        			try {
        				line_length = doc.getLineLength(line_num);
        				line_offset = doc.getLineOffset(line_num);
        				if (eol) line_length -= 1;
        			} catch (BadLocationException e1) {
        				e1.printStackTrace();
        			}
		            // Replace the code snippet back into the document.
		            doc.replace(line_offset, line_length, fixed_code.get(0));
		            previous_offset = line_offset;
		            previous_length = fixed_code.get(0).length();
		            
		            //Move cursor to the end of the inserted snippet.
		            editor.selectAndReveal(previous_offset + previous_length, 0);
		            doc.addDocumentListener(InputHandler.doclistener);
		        } catch (BadLocationException e) {
					System.out.println("Error with getting input query.");
					e.printStackTrace();
					return null;
				}
		    }
	    }
		return null;
	}
	
	/* 
	 * Function fixSpacing
	 *   Given a list of code snippets, and a fixed offset (spacing) for where the code snippet insertion starts,
	 *     add the fixed offset to each line of each code snippet.
	 *   Essentially, this function fixes alignment issues when inserting code snippets at an offset.
	 *   
	 *   Inputs: Vector<String> queries - vector of different code snippets to insert into the document.
	 *   		 String spacing - Offset of query to be applied to each code snippet.
	 *   
	 *   Retuns: Vector<String> - vector of code snippets with fixed offset.
	 */
	private static Vector<String> fixSpacing(Vector<String> queries, String spacing) {
		Vector<String> fixed_queries = new Vector<String>();
		
		for (int i=0; i<queries.size(); i++) {
			BufferedReader bufReader = new BufferedReader(new StringReader(queries.get(i)));
			String line;
			String new_query = "";
			try {
				while ( (line=bufReader.readLine()) != null) {
					line = spacing + line + "\n";
					new_query += line;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			fixed_queries.add(new_query);
		}
		
		return fixed_queries;
	}
}