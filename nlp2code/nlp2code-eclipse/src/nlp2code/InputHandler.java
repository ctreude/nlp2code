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

// Create a class handler to run a function when an event is triggered. 
public class InputHandler extends AbstractHandler {
	
	static Vector<String> previous_search = new Vector<String>();
	static String previous_query = "";
	static int previous_offset = 0;
	static int previous_length = 0;
	static Vector<String> previous_queries = new Vector<String>();
	static CycleDocListener doclistener = new CycleDocListener();
	static QueryDocListener qdl = new QueryDocListener();
	static Vector<IDocument> documents = new Vector<IDocument>();
	static boolean feedback = true;
	// Function to be ran when the handler is triggered/ instantiated.
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ITextEditor current_editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument current_doc = current_editor.getDocumentProvider().getDocument(current_editor.getEditorInput());
		if (!InputHandler.documents.contains(current_doc)) {
			current_doc.addDocumentListener(InputHandler.qdl);
		}
		//TODO more null-checking, empty query checking etc.       
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
		            /*
		            if (text.equals("toggle_nlp2code_feedback")) {
		            	if (InputHandler.feedback == false) {
		            		InputHandler.feedback = true;
		            	} else {
		            		InputHandler.feedback = false;
		            	}
		            	
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
			            doc.replace(line_offset, line_length, "");
		            	return null;
		            }
		            */
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