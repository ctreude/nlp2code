/*****cs.adelaide.nlp2code Eclipse plugin*****/

/*   Plugin Installation Instructions:   */

Before you start installing the plugin, make sure you have Eclipse Neon for Java EE developers. The plugin is not guaranteed to be compatibale with other Eclipse versions.

Also, to load this project into eclipse, just add the nlp2code-eclipse directory to your eclipse workspace and refresh the workspace.

// STEP 1: INSTALLING THE PLUGIN AND RUNNING IT ONCE:
1. Move the cs.adelaide.nlp2code directory to a suitable place.
	- One recommended place is the "plugins" section of your eclipse installation. Remember this directory as you'll need it later.
2. Launch Eclipse and navigate to Help->Install New Software...
3. In the "Work with:" section, add a new location by clicking Add...->Local...
4. Navigate to the cs.adelaide.nlp2code directory and click OK.
5. Click OK to close the Add Repository popup.
6. Check the "Nlp2code" check box and click Next>
7. Wait for a while while the dependencies are calculated. The install details should pop up and the Nlp2code package should be listed. Click Next>
8. Accept the terms of the license agreement and click Finish.
9. Press "OK" to the Security Warning popup (I haven't signed the package yet).
10. Restart Eclipse.
11. For the content assist portion of the plugin to work optimally, you need to add a key binding to activate it specifically. To do this, follow these instructions (Linux and Windows):
	- Navigate to Window->Preferences.
	- Navigate to General->Keys.
	- Search for "nlp2code" and click on Content Assist(nlp2code.contentassist).
	- Click in the box next to "Binding:" and enter a key sequence for the binding of your choosing. It is recommended to use ctrl+1 (holding control and pressing the one key), as this does not conflict with other eclipse bindings.
For Mac, you can follow these instructions to set a binding:
	- Go to eclipse (top left corner) -> preferences.
	- Navigate to General->Keys.
	- In the search bar type "nlp2code" and select the result.
	- In the "Binding" box below the search results, click on the bar and enter a key sequence for the binding of your choosing. It is reccomanded to use command+1 (holding command and pressing 1). The binding should show up in the box, then you can unpress both buttons.
Test to see whether the content assist works by pressing the hotkey you made. You will know it is working when the content assist box opens and there are natural language queries inside of it (e.g. sort an array?).

// STEP 2: ADDING A GOOGLE CUSTOM SEARCH ENGINE:
	This project makes use of Google's Custom Search Engine (CSE) Application Programming Interface (API). Due to limitations with the service, only 100 calls to the API (i.e. 100 uses of the plugin) will be allowed for free each day. This is shared amongst all users, hence you need to create and link your own Google CSE (you will need a google account for this).

1. Navigate to https://cse.google.com.au/cse/
2. Click "Create a custom search engine".
3. In the sites to search text box, enter: http://stackoverflow.com/
4. In the name of the search engine text box, enter any name you want.
5. Click the Control Panel button.
6. Click on the Search engine ID button and copy and store this somewhere for later.
7. Navigate to https://console.developers.google.com/
8. Accept the Terms of Service. You can decline the E-mail updates section. Press Agree and Continue.
9. Create a new project by navigating to My Project->Create project.
10. In the API Manager tab on the left, navigate to Dashboard.
11. In the search bar, search for "custom search" and click Custom Search API.
12. Click the Enable button to enable the custom search API.
13. In the API Manager tab on the left, navigate to Credentials.
14. Click Create->API key to create an api key.
15. Copy the API key on the screen and store this somewhere for later.
16. Navigate to your local Eclipse installation and go into the plugins folder in the root directory.
17. Find the folder cs.adelaide.nlp2code and the file preferences.txt.
18. Open preferences.txt and next to the "APIKEY=" part, paste the API key you saved in step 12 (replace the one that is there already).
19. In preferences.txt, next to the  "CUSTOM_SEARCH_ID=" part, paste the search engine ID you saved in step 6 (replace the one that is there already).
20. All Done! The next time you load up the nlp2code plugin in eclipse, it should load up the new search engine information. 

Note that for this configuration, you only get 100 queries per day. This is because Google limits API calls to 100 per day if it is being used for free. After 100 queries, other queries will fail saying that there is a connection problem. To increase this quota, you need to navigate to your custom search engine on https://cse.google.com.au/cse/ and go to billing to pay for more queries per day.

Uninstallation information is on the bottom of this readme.


/*  How to use the plugin:   */

There are many ways to activate and use the plugin. 
Firstly, ensure that you are connected to the internet as this is needed to get Stack Overflow data.
To find a code snippet for a task you want to achieve, you can conduct natural language queries (note that the default language is Java for searching, however you can specifcy a different language by putting "in language" at the end of your query, e.g. "sort an array in python").

Queries can be invoked in three ways:
	1. Write your query on a line (e.g. sort an array) and press the Stack Overflow button on the toolbar (or pressing ctrl+6 as a hotkey).
	2. Construct a natural language query of characters and spaces with a question mark at the end (e.g. sort an array?).
	3. Pressing the keys you binded for the content assist during the setup to open the content assist and select one of the recommended tasks from the list. The results can be filtered by typing and similar tasks will be listed. If you chose not to set up a binding, you can cycle through content assist suppliers until you get to nlp2code.contentassist by pressing ctrl+space.

Once a query is invoked, the plugin will take a few seconds to collect and rank code snippets by relevance to your query. Once it is finished, the top result will be pasted into your document. If you find that the first result isn't helpful, you can cycle through all of the retrieved code snippets by pressing ctrl+` (ctrl + tilde/backtick key), or by pressing the stack overflow button with the blue arrow on the toolbar. Once you edit the document after a snippet has been pasted, you will be prompted for feedback on whether the query was successful or not.

tl;dr:
Conduct a query by:
	1. highlighting the text and pressing ctrl+6 (or pressing the stack overflow button).
	2. writing a query comprised of letters and spaces (no other characters accepted). e.g. sort an array?
	3. selecting a task in the content assist that suits what you are looking for.
After a query, cycle through possible solutions with ctrl + `.
After you select a snippet, you will be prompted for feedback if feedback has been enabled in the preferences.txt


/*   Plugin Uninstall Information:   */

I understand that you may not want random plugins to remain on your workspace. Uninstalling is much simpler than installing. To do this, follow these steps:

1. Navigate to Help->Installation details... in the menu bar.
2. Filter by "nlp2code" or scroll down to find nlp2code.
3. Select nlp2code and press Uninstall.
4. Press "Finish" on the next window.
5. Restart Eclipse and the plugin should now be gone!