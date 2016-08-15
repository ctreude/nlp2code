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
		        ISelection sel = editor.getSelectionProvider().getSelection();
		        // If we find that the current selection is selecting text (only)
		        if ( sel instanceof TextSelection ) {
		            final TextSelection textSel = (TextSelection)sel;
		            
		            // Pull the string of text that was selected .
		            String text = textSel.getText();
		            
		            
		            //LanguageProcessor processor = new LanguageProcessor();
		            //String[] tags = processor.process(text);
		            //String newText = "";
		            //for (int i=0; i<tags.length; i++) {
		            // 	newText += tags[i];
		            //}
		            String newText = "/*" + text + "*/";
		            
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
