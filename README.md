# NLP2Code Eclipse plugin

[![NLP2Code video](https://img.youtube.com/vi/h-gaVYtCznI/0.jpg)](https://www.youtube.com/watch?v=h-gaVYtCznI)

## Plugin Installation Instructions:

To install the plugin for development:
 1. Download and install the Eclipse SDK from the Eclipse Project page.
 2. Install Git Integration (EGit) for the Eclipse SDK.
 3. File->Import->Git->Projects from Git->Clone URI.
 4. Copy and paste the .git URI from the NLP2Code GitHub.
 5. Press Next until you get to the project import wizard. Choose "Import exisiting Eclipse projects" and press Next and Finish.
 6. You can now run the plugin by setting the Run Configuration to run as an Eclipse Application.

To install the plugin on your regular Eclipse environment (e.g. for personal use), you will need to package the plugin so it can be installed via the Eclipse Install New Software tool. Since this repository is purely for the development of the tool, there is currently no support in this repository for packaging the plugin for installation.


## Important Plugin Configuration Settings:

Content Assist:
To get the most out of the plugin, it is strongly recommended to add a content assist binding to trigger the NLP2Code task content assist window. You can do this by navigating to: Preferences->Java->Editor->Content Assist (Path may be different depending on Eclipse version) and adding a '?' symbol to the set of symbols that trigger content assist.

Google Custom Search Engine (CSE):
Currently, the plugin uses Google's Custom Search Engine API to collect StackOverflow forum threads. The free version of this API is limited to 100 requests a day (shared between all users of the plugin). It is recommended that you either sign up for and create your own Google Custom Search Engine to relax this hard limit. To add your Google CSE to the plugin, edit the appropriate "key" and "cx" variables in Searcher.java with your Google API key and your Google CSE ID.
Full instructions on how to get your own Google CSE is in the "GoogleCSEInstructions.txt" file.


## How to use the plugin:

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

## How to contribute:

Pull requests are most welcome!

## References:

See http://cs.adelaide.edu.au/~christoph/icsme17c.pdf for more information
