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
 * The activator class controls the plug-in life cycle.
 *   The Activator class is the first class to be instsantiated whenever the plugin is invoked.
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "nlp2code";

	// The shared instance
	private static Activator plugin;

	/*
	 *  Constructor.
	 *  Initializes defaults for document listeners, loads the list of tasks from the local database, and
	 *  saves the Google Custom Search engine defaults to a preferences file.
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
		TaskRecommender.loadTasks();
		saveState();
		loadState();
	}

	/*
	 * Function start
	 *   Called when the plugin starts.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * Function stop
	 *   Called when the plugin stops.
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/*
	 * Function getDefault
	 *   Returns the shared instance (Activator object).
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/*
	 * Function loadState
	 *   Loads default information from preferences.txt file.
	 *   Specifically loads the custom search engine ID and google API key to use for making google CSE searches.
	 */
	public static void loadState() {
		// Open up defaults from preferences.txt file
		try (BufferedReader br = new BufferedReader(new FileReader("plugins/nlp2code/preferences.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       String[] s = line.split("=");
		       if (s.length > 0) {
		    	   if (s[0].equals("APIKEY")) {
		    		   Searcher.key = s[1];
		    	   } else if (s[0].equals("CUSTOM_SEARCH_ID")) {
		    		   Searcher.cx = s[1];
		    	   }
		       }
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/* 
	 * Function saveState
	 *   Saves default API key and custom search ID information to the preferences.txt file.
	 */
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
		
		// Load file to see if anything is in there.
		String result = "";
		try (BufferedReader br = new BufferedReader(new FileReader("plugins/nlp2code/preferences.txt"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	result += line;
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		// If file has stuff in it, don't overwrite it.
		if (!result.equals("")) return;
		
		// Save default preferences.
		try {
			Files.write(Paths.get(file_path), text.getBytes() , StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Function makeDir
	 * 	 Given a relative directory filepath, create the directory.
	 * 
	 * 	 Input: File theDir - Relative directory filepath.
	 */
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