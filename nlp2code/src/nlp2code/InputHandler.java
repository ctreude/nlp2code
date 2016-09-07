package nlp2code;

import java.util.Vector;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

// Create a class handler to run a function when an event is triggered. 
public class InputHandler extends AbstractHandler {
	// Function to be ran when the handler is triggered/ instantiated.
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//TODO more null-checking, empty query checking etc.
		try {               
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
		        if (sel.isEmpty()) return null;
		        // If we find that the current selection is selecting text (only)
		        if ( sel instanceof TextSelection ) {
		            final TextSelection textSel = (TextSelection)sel;
		            
		            // Pull the string of text that was selected.
		            String text = textSel.getText();
		            if (text.equals("")) return null;

		            // Get the vector of URLS from the getPosts query.		            
		            Vector<String> results = Searcher.getPosts(text);
		            if (results.size() == 0) return null;
		            
		            // Get the accepted answer code snippet.
		            String newText = Searcher.getCodeSnippet(results);
		            if (newText.equals("")) return null;
		            		            
		            // Replace the code snippet back into the document.
		            doc.replace( textSel.getOffset(), textSel.getLength(), newText );
		            
		            //Highlight the updated snippet to show the insertion.
		            ISelection new_select = new TextSelection(textSel.getOffset(),newText.length());
		            editor.getSelectionProvider().setSelection(new_select);
		        }
		    }
		} catch ( Exception ex ) {
		    ex.printStackTrace();
		    return null;
		}
		return null;
	}
}
