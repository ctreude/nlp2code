package nlp2code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class QueryDocListener implements IDocumentListener {		
		
		@Override
        public void documentChanged(DocumentEvent event) 
        {
			// This is the part of the code where we format the event (either encounter a ? xxx ? or xxxx? or auto-complete scenario, this will format and isolate
			// query in 'line' and search for code snippets using query.
    		String insertion = event.getText();
    		if (insertion == "") return;
    		String check_undo = getLine();
    		check_undo = check_undo.trim();
    		if (check_undo.startsWith("?")) check_undo = check_undo.substring(1);
    		if (check_undo.endsWith("?")) check_undo = check_undo.substring(0, check_undo.length()-1);
    		if (InputHandler.previous_queries.contains(check_undo)) {
				InputHandler.previous_queries.remove(InputHandler.previous_queries.indexOf(check_undo));
				return;
    		}
				        		
    		if (insertion.length() >= 1)
    		{
    			String line = getLine();
    			String newline = line.trim();
    			if (!(newline.endsWith("?"))) return;
    			int result = doQuery(event,line);
    			if (result != 0) {
    				//JOptionPane.showMessageDialog(null, "Query Unsuccessful, exit code: " + result, "WARNING!", JOptionPane.INFORMATION_MESSAGE);
    			}
    		}
        }
		
		@Override
        public void documentAboutToBeChanged(DocumentEvent event) 
        {
        }
		
		private static int doQuery(DocumentEvent event, String line) {
			String whitespace_before = line.substring(0, line.indexOf(line.trim()));
			line = line.trim();
			if (line.endsWith("?")) line = line.substring(0, line.length()-1);
			if (line.startsWith("?")) line = line.substring(1);
			line = line.trim();
			if (line.length() == 0) return 1;
			line = line.toLowerCase();
			if (!line.matches("[abcdefghijklmnopqrstuvwxyz ]*")) return 2;
			//final IDocument document = event.getDocument();
			IEditorPart epart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			ISelectionProvider selectionProvider = ((ITextEditor)epart).getSelectionProvider();
			if (selectionProvider.equals(null)) return 3;
			ISelection selection = selectionProvider.getSelection();
			if (selection.equals(null)) return 4;
			ITextEditor ite = (ITextEditor)epart;
			if (ite.equals(null)) return 5;
			// Get the current document (for isolating substring of text in document using line number from selection).
			IDocument document = ite.getDocumentProvider().getDocument(ite.getEditorInput());
			Vector<String> url = new Vector<String>();
			if (JavaCompletionProposalComputer.queries_map.containsKey(line)) {
				String[] urls = JavaCompletionProposalComputer.queries_map.get(line).split(",");
				if (urls.length == 0) return 6;
				Vector<String> SO_urls = getUrls(urls);
				for (int i=0; i<SO_urls.size(); i++) {
					if (i == Searcher.NUM_URLS) break;
					url.add(SO_urls.get(i));
				}
				if (url.size() < 5 && JavaCompletionProposalComputer.query_task) {
					Vector<String> result = Searcher.getThreads(line);
					if (!(result.size() == 0)) {
						for (int i=0; i<result.size(); i++) {
					    	if (i-1 == Searcher.NUM_URLS) break;
					    	url.add(result.get(i));
					    }
					}
				}
				//System.out.println("USED QUERYMAP");
			} else {
				Vector<String> result = Searcher.getThreads(line);
			    if (result.size() == 0) return 7;
			    for (int i=0; i<result.size(); i++) {
			    	if (i == Searcher.NUM_URLS) break;
			    	url.add(result.get(i));
			    }
			    //System.out.println("USED SEARCHER");
		    }
			//System.out.println(url.toString());
		    Vector<String> code = Searcher.getCodeSnippets(url);
		    if (code.size() == 0) return 8;
		    if (code.equals(null)) {
		    	System.out.println("Error! Code vector is null!");
		    	return 9;
		    }
		    Vector<String> fixed_code = fixSpacing(code,whitespace_before);
		    InputHandler.previous_search.clear();
		    InputHandler.previous_search = fixed_code;
		    InputHandler.previous_query = line;
		    InputHandler.previous_queries.add("?" + line + "?");
		  	// Get the line number we are currently on.
	      	try {
	      		int e_offset = event.getOffset();
	      		int line_num = document.getLineOfOffset(e_offset);
	      		int l_offset = document.getLineOffset(line_num);
	      		int l_length = document.getLineLength(line_num);
	      		String replacement_text = fixed_code.get(0);
	      		//JOptionPane.showMessageDialog(null, "Query Successful! Press ctrl+` to cycle through answers.", "Information!", JOptionPane.INFORMATION_MESSAGE);
	      		if (l_offset < 0 || l_offset > document.getLength()) return 10;
	      		if (l_length > document.getLength() || l_offset + l_length > document.getLength()) return 11;
	      		if (document.equals(null)) System.err.println("ERROR, NULL DOC");
	      		Display.getDefault().asyncExec(new Runnable() 
	      	    {
	      	      public void run()
	      	      {
	      	    	try {
	      	    		ITextEditor editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	      				IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
						doc.replace(l_offset, l_length, replacement_text);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
	      	      }
	      	    });
	      		Display.getDefault().asyncExec(new Runnable()
	      		{
	      			public void run()
	      			{
	      				ITextEditor editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	      				IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
						String document = doc.get();
						InputHandler.previous_offset = document.indexOf(replacement_text);
	      			}
	      		});
	      		document.addDocumentListener(InputHandler.doclistener);
	      		InputHandler.previous_length = replacement_text.length();
	      		ITextEditor editor = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	      		editor.selectAndReveal(l_offset+replacement_text.length(), 0);
	      	} catch (BadLocationException e) {
				System.err.println("Error with inserting code after Autocomplete");
				e.printStackTrace();
			} catch (IllegalStateException e) {
				System.err.println("ILLEGAL STATE EXCEPTION CRAP");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.err.println("ILLEGAL ARGUMENT EXCEPTION CRAP");
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.err.println("NULL POINTER EXCEPTION"); 
				e.printStackTrace();
			}
	      	return 0;
		}
		
		private static Vector<String> getUrls(String[] urls) {
			Vector<String> google_urls = new Vector<String>();
			for (int i=0; i<urls.length; i++) {
				google_urls.add("http://stackoverflow.com/questions/" + urls[i]);
			}		
			return google_urls;
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
		
		private static String getLine() {
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			IDocument doc;
			int offset;
			String line = "";
			// If we are dealing with a text editor. 
			if(editor instanceof ITextEditor) {
				// Get the current selection of the document (for determining what line a query is on).
				ISelectionProvider selectionProvider = ((ITextEditor)editor).getSelectionProvider();
			    if (selectionProvider.equals(null)) return "";
			    ISelection selection = selectionProvider.getSelection();
			    if (selection.equals(null)) return "";
			    ITextEditor ite = (ITextEditor)editor;
			    if (ite.equals(null)) return "";
			    // Get the current document (for isolating substring of text in document using line number from selection).
			    doc = ite.getDocumentProvider().getDocument(ite.getEditorInput());
			    if (doc.equals(null)) return "";
			    
			    if (selection instanceof ITextSelection) {
			        ITextSelection textSelection = (ITextSelection)selection;
			        try {
			        	// Get the line number we are currently on.
			        	if (textSelection.equals(null)) return "";
			        	offset = textSelection.getOffset();
			        	// Get the string on the current line and use that as the query line to be auto-completed.
			        	if (offset > doc.getLength() || offset < 0) return "";
						line = doc.get(doc.getLineOffset(doc.getLineOfOffset(offset)), doc.getLineLength(doc.getLineOfOffset(offset)));
					} catch (BadLocationException e) {
						System.out.println("Error with getting input query.");
						e.printStackTrace();
						return "";
					}
			    }
			} else {
				// If we are not dealing with a text editor.
				return "";
			}
			
			return line;
		}
}
