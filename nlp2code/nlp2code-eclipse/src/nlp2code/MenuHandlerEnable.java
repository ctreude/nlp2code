package nlp2code;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.IDocument;

//TODO Null checks for everything.
//TODO IMPLEMENT UNDO/REDO FUNCTIONALITY, currently bugged?

public class MenuHandlerEnable extends AbstractHandler {

	// Function to add document listeners to the passed IDocument.
	// Prints to the console every time an event happens (e.g. type, backspace, select and delete, copy+paste)
	// May be useful for running code on a document automatically later.
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        return null;
	}
	
	static void addQueryListener() {
		ITextEditor editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.addDocumentListener(InputHandler.qdl);
		InputHandler.documents.add(doc);
	}
}
