package nlp2code;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

/**
 * class CycleDocListener
 *   Document listener that listens for when a document is edited.
 *   When a document is edited, it notified cycleAnswerHandler to disallow
 *   cycling through answers anymore.
 */
public class CycleDocListener implements IDocumentListener {
	
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {		
	}

	/*
	 * Function documentChanged
	 *   Called when a document is changed.
	 *   Disables cycling through code snippets by signalling cycleAnswersHandler when the document has been edited.
	 */
	@Override
	public void documentChanged(DocumentEvent event) {
		String text = event.getText();
		if (!InputHandler.previous_search.contains(text)) {
			IDocument doc = event.getDocument();
			doc.removeDocumentListener(InputHandler.doclistener);
			CycleAnswersHandler.changed_doc = true;
		}
	}
}