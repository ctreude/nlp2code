package nlp2code;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
// For dealing with procedurally checking document:
// http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fui%2FIStartup.html
// http://stackoverflow.com/questions/26871970/eclipse-plugin-development-how-to-listen-events-in-eclipse-editor
// http://stackoverflow.com/questions/8284391/eclipse-plugin-to-read-contents-of-a-editor

import java.util.Vector;
	// Vector<String> myVector = new Vector<String>();
	// myVector.add(sample);
	// myVector.insertElementAt(sample, index);
	// String sample = myVector.elementAt(0);
	//  - used in assignment of elements taken from vector
	// myVector.get(0);
	// myVector.remove(0);
	// String holder = myVector.toString();
	//  - converts entire vector to string representation for bug testing
	// myVector.size();


// Create a class handler to run a function when an event is triggered. 
public class InputHandler extends AbstractHandler {

	// Function to be ran when the handler is triggered/ instantiated.
	// Code shell to get text selection obtained from:
	// http://stackoverflow.com/questions/1694748/adding-item-to-eclipse-text-viewer-context-menu
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {               
			// Get the current active editor.
		    IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    // If the current active editor is a text editor (i.e. not any other workbench component)
		    if ( part instanceof ITextEditor ) {
		    	// Use text editor context to find the current selection of the user
		        final ITextEditor editor = (ITextEditor)part;
		        IDocumentProvider prov = editor.getDocumentProvider();
		        IDocument doc = prov.getDocument( editor.getEditorInput() );
		        
		        // For text solution stuff:
		        // help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjface%2Ftext%2FTextSelection.html
		        ISelection sel = editor.getSelectionProvider().getSelection();
		        // If we find that the current selection is selecting text (only)
		        if ( sel instanceof TextSelection ) {
		            final TextSelection textSel = (TextSelection)sel;
		            
		            // Pull the string of text that was selected .
		            String text = textSel.getText();
		            
		            // Make a new processor class to abstract the query processing away from the handler.
		            LanguageProcessor processor = new LanguageProcessor();
		            Vector<String> tags = processor.process(text);
		            String newText = tags.toString();
		            
		            //newText = "/*" + text + "*/";
		            
		            // Replace the code snippet back into the document.
		            doc.replace( textSel.getOffset(), textSel.getLength(), newText );
		        }
		    }
		} catch ( Exception ex ) {
		    ex.printStackTrace();
		}	
		return null;
	}
}
