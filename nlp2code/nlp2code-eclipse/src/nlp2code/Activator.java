package nlp2code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Vector;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "nlp2code"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		ITextEditor editor = (ITextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		doc.addDocumentListener(InputHandler.qdl);
		InputHandler.documents.add(doc);
		InputHandler.previous_search = new Vector<String>();
		InputHandler.previous_offset = 0;
		InputHandler.previous_length = 0;
		InputHandler.previous_queries = new Vector<String>();
		InputHandler.doclistener = new CycleDocListener();
		JavaCompletionProposalComputer.loadTasks();
		saveState();
		loadState();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void loadState() {
		try (BufferedReader br = new BufferedReader(new FileReader("plugins/nlp2code/preferences.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       String[] s = line.split("=");
		       if (s.length > 0) {
		    	   if (s[0].equals("APIKEY")) {
		    		   Searcher.key = s[1];
		    	   } else if (s[0].equals("CUSTOM_SEARCH_ID")) {
		    		   Searcher.cx = s[1];
		    	   } else if (s[0].equals("FEEDBACK")) {
		    		   if (s[1].equalsIgnoreCase("false"))
		    			   InputHandler.feedback = false;
		    		   else if (s[1].equalsIgnoreCase("true"))
		    			   InputHandler.feedback = true;
		    		   else 
		    			   InputHandler.feedback = true;
		    	   }
		       }
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static void saveState() {
		String text = "APIKEY=AIzaSyClq5H_Nd7RdVSIMaRPQhwpG5m_-68fWRU\nCUSTOM_SEARCH_ID=011454571462803403544:zvy2e2weyy8\nFEEDBACK=true";
		File dir = new File("plugins");
		if (!dir.exists()) makeDir(dir);
		File dir1 = new File("plugins/nlp2code");
		// if the directory does not exist, create it
		if (!dir1.exists()) makeDir(dir1);
		String file_path = dir1.getAbsolutePath() + "/preferences.txt";
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
		
		String result = "";
		try (BufferedReader br = new BufferedReader(new FileReader("plugins/nlp2code/preferences.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	result += line;
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!result.equals("")) return;
		
		try {
			Files.write(Paths.get(file_path), text.getBytes() , StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void makeDir (File theDir) {
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