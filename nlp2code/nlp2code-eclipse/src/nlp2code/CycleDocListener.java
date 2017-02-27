package nlp2code;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.swing.JOptionPane;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

public class CycleDocListener implements IDocumentListener {

	private String prev_code_snippet = "";
	private String query = "";
	
	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {		
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		String text = event.getText();
		if (!InputHandler.previous_search.contains(text)) {
			IDocument doc = event.getDocument();
			if (InputHandler.feedback == true) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						int dialogResult = JOptionPane.showConfirmDialog (null, "Was the code snippet found helpful?","Feedback Required",JOptionPane.YES_NO_OPTION);
						if (dialogResult == JOptionPane.YES_OPTION ) {
							String result = "YES";
							saveQueryResult(result);
						} else if (dialogResult == JOptionPane.NO_OPTION) {
							String result = "NO";
							saveQueryResult(result);
						} else { }
					}
				});
			}
			doc.removeDocumentListener(InputHandler.doclistener);
			CycleAnswersHandler.changed_doc = true;
		} else {
			prev_code_snippet = text;
			query = InputHandler.previous_query;
		}
	}
	
	private void saveQueryResult(String result) {
		File dir1 = new File("plugins/nlp2code");
		// if the directory does not exist, create it
		if (!dir1.exists()) makeDir(dir1);
		String file_path = dir1.getAbsolutePath() + "/saved_queries.txt";
		File dir2 = new File(file_path);
		dir2.getParentFile().mkdirs();
		try {
			if (!dir2.exists());
				dir2.createNewFile();
		} catch (IOException e1) {
			System.out.println("ERROR MAKING FILE");
			e1.printStackTrace();
			return;
		}
		
		try {
			if (prev_code_snippet.indexOf("\n") == -1) {
				prev_code_snippet = InputHandler.previous_search.get(0);
				query = InputHandler.previous_query;
			}
			String text = "< query=\"" + query + "\" result=\"" + result + "\" snippet=\"" + prev_code_snippet.substring(0, prev_code_snippet.indexOf("\n")) + "\" snippetIndex=\"" + CycleAnswersHandler.previous_index + "\" >\n";
			Files.write(Paths.get(file_path), text.getBytes() , StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makeDir (File theDir) {
		System.out.println("Creating DIR");
	    boolean result = false;
	    try{
	        theDir.mkdir();
	        result = true;
	    } 
	    catch(SecurityException se){
	    	se.printStackTrace();
	    }        
	    if(result) {    
	        System.out.println("DIR created");  
	    }
	}
}