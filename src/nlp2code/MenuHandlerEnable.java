package nlp2code;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jface.text.IDocument;

/**
 * class MenuHandlerEnable
 *   Implements required functionality to add a query listener to the current document.
 *   The query listener listens for changes in the document to identify if a ?{query}? format query happens.
 */
public class MenuHandlerEnable extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        return null;
	}
	
	/*
	 * Function addQueryListener
	 *   Adds a QueryDocListener object to the current document and adds the document to the list of documents
	 *   that currently have the QueryDocListener applied to it.
	 */
	static void addQueryListener() {
		ITextEditor editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.addDocumentListener(InputHandler.qdl);
		InputHandler.documents.add(doc);
	}
}
