package caret.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import caret.Activator;
import caret.ChatView;
import caret.data.MongoDB;
import caret.data.PluginDocument;
import caret.data.PluginRepository;
import caret.preferences.PreferenceConstants;
public class ClearHandler {

    @Execute
    public void execute(Shell shell){
        ChatView chatView = ChatView.getInstance();
        if (chatView != null) {
            chatView.clearChatSession();
        } else {
            System.err.println("ChatView instance is null.");
        }
    }
    
    public void db() {
        // 1. Setup the connection
    	IPreferenceStore store;
    	String mongoURI;
    	String mongoDatabase;
    	store = Activator.getDefault().getPreferenceStore();
    	mongoDatabase = store.getString(PreferenceConstants.P_MONGO_DATABASE);
    	mongoURI  = store.getString(PreferenceConstants.P_MONGO_URI);
		if (mongoURI != null && !mongoURI.isEmpty()  && mongoDatabase != null && !mongoDatabase.isEmpty()) {
			MongoDB mongoDB = new MongoDB();
	    	mongoDB.setDatabase(mongoDatabase);
	    	mongoDB.setConnectionURI(mongoURI);
	    	if(mongoDB.connect()) {
	    		mongoDB.setupCollection("plugins");
	 	        
	            PluginRepository repository = new PluginRepository(mongoDB);

	            System.out.println("\n--- [1] Creating New Plugin ---");
	            PluginDocument myPlugin = new PluginDocument();
	            myPlugin.setId("notion-connector");
	            myPlugin.setName("Notion Sync");
	            myPlugin.setVersion("1.0.0");
	            myPlugin.setProvider("Caret Labs");
	            myPlugin.setDescription("Syncs tasks with Notion databases.");
	            myPlugin.setInstallCount(0);

	            repository.savePlugin(myPlugin);

	            System.out.println("\n--- [2] Retrieving Created Plugin ---");
	            PluginDocument fetched = repository.getPluginByIdAndVersion("notion-connector", "1.0.0");
	            
	            if (fetched != null) {
	                System.out.println("Read Result: " + fetched.getName() + " by " + fetched.getProvider());
	                System.out.println("Current Description: " + fetched.getDescription());
	            }
	            
	            System.out.println("\n--- [3] Updating Existing Plugin ---");
	            fetched.setDescription("Updated: Now supports bidirectional sync!");
	            fetched.setInstallCount(150);
	            
	            repository.savePlugin(fetched);

	            System.out.println("\n--- [4] Verifying Update ---");
	            PluginDocument updatedVersion = repository.getPluginByIdAndVersion("notion-connector", "1.0.0");
	            
	            if (updatedVersion != null) {
	                System.out.println("Verified Description: " + updatedVersion.getDescription());
	                System.out.println("Verified Install Count: " + updatedVersion.getInstallCount());
	            }

	            System.out.println("\n--- [5] Creating a New Version (v1.1.0) ---");
	            PluginDocument nextVersion = new PluginDocument();
	            nextVersion.setId("notion-connector"); 
	            nextVersion.setVersion("1.1.0");        
	            nextVersion.setName("Notion Sync PRO");
	            nextVersion.setProvider("Caret Labs");
	            
	            repository.savePlugin(nextVersion);

	            System.out.println("\nProcess finished successfully.");
	    		
	 	        mongoDB.close();
	    	}
	       
		}
    }
}