package nlp2code;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class CycleAnswersHandler extends AbstractHandler {

	static int previous_index = 0;
	static boolean changed_doc = false;
	//TODO null checking, out of bounds checking, etc.
	//TODO make a message/feedback box when the user presses enter or starts typing something else. (a key listener maybe on the document, remove it afterwards for each search).
		//Use joptionpane for making a dialogue box for user input.
		//use swingutilities.invokelater to test to see if it works OR use the key difference thing.
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		if (changed_doc == true) {
			changed_doc = false;
			InputHandler.previous_length = 0;
			InputHandler.previous_offset = 0;
			return null;
		}
		try {
			// Get the current active editor.
		    IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    
		    // If the current active editor is a text editor (i.e. not any other workbench component)
		    if ( part instanceof ITextEditor ) {
		    	// Use text editor context to locate the document of the editor and get the input stream to that editor.
		        ITextEditor editor = (ITextEditor)part;
		        IDocumentProvider prov = editor.getDocumentProvider();
		        IDocument doc = prov.getDocument( editor.getEditorInput() );
		        int offset = InputHandler.previous_offset;
		        int length = InputHandler.previous_length;
		        if (offset + length > doc.getLength()) return null;
		        String text = doc.get(offset, length);
				if (InputHandler.previous_search.contains(text)) {
		        	int index = InputHandler.previous_search.indexOf(text);
		        	index++;
		        	if (index >= InputHandler.previous_search.size() || index < 0) index = 0;
		        	doc.replace(offset, length, InputHandler.previous_search.get(index));
		            InputHandler.previous_length = InputHandler.previous_search.get(index).length();
		            previous_index = index;
		            if (InputHandler.previous_offset + InputHandler.previous_length > doc.getLength()) return null;
		            editor.selectAndReveal(InputHandler.previous_offset + InputHandler.previous_length, 0);
		            return null;
		        }
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
