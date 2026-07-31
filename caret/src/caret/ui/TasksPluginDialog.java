package caret.ui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Document;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.mongodb.client.model.Filters;

import caret.Activator;
import caret.ChatView;
import caret.RemoteP2Reader;
import caret.data.MongoDB;
import caret.data.PluginData;
import caret.data.PluginDocument;
import caret.data.PluginRepository;
import caret.preferences.PreferenceConstants;
import caret.repository.P2Installer;
import caret.repository.PluginInstaller;
import caret.repository.PluginInstallerP2;

public class TasksPluginDialog extends Dialog {
	
	ChatView chatView = ChatView.getInstance();
	String pluginId = null;

	public TasksPluginDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Assistance Tasks");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        Label label = new Label(container, SWT.NONE);
        label.setText("The available tasks are:");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));

        ScrolledComposite sc = new ScrolledComposite(container, SWT.V_SCROLL | SWT.BORDER);
        GridData scData = new GridData(SWT.FILL, SWT.FILL, true, true);
        scData.heightHint = 300;
        sc.setLayoutData(scData);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        Composite content = new Composite(sc, SWT.NONE);
        content.setLayout(new GridLayout(1, false));
        
        Label lblMessage = new Label(container, SWT.NONE);
        lblMessage.setText("  ");
        lblMessage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
        
        Map<String, PluginData> plugins = null;
        RemoteP2Reader r2pr = new RemoteP2Reader();
        IPreferenceStore store;
        store = Activator.getDefault().getPreferenceStore();
        String p2URL = store.getString(PreferenceConstants.P_TASKS_P2_REPOSITORY_URL);
        try {
        	lblMessage.setText("Connecting to P2 repository");
			plugins = r2pr.getTasksPlugins(p2URL);
			lblMessage.setText("  ");
			System.out.println("Extracting:"+plugins.size());
		} catch (Exception e) {
			lblMessage.setText("Error connecting to P2 repository");
			//e.printStackTrace();
			return container;
		}
        if(plugins==null || plugins.size()==0) {
			lblMessage.setText("Could not retrieve plugins");
		}else {
			plugins.forEach((id, data) -> {
				System.out.println("plugin:"+id+"-"+data.getName());
				if(pluginId !=null) {
					if(pluginId.equals(id)) {
					    createPluginItem(content, data);
					    //db(data);
					}
				}else {
				    createPluginItem(content, data);
				    //db(data);
				}
			});
			
		}
        sc.setContent(content);
        sc.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        return container;
    }

    private void createPluginItem(Composite parent, PluginData pluginData) {
        Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        int totalInstall=0;
        totalInstall = getPluginInstallCount(pluginData.getId());
        Group itemGroup = new Group(parent, SWT.NONE);
        itemGroup.setBackground(white);
        itemGroup.setLayout(new GridLayout(2, false));
        itemGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        StyledText st = new StyledText(itemGroup, SWT.WRAP | SWT.READ_ONLY);
        String pluginName = pluginData.getName();
        String details = "\n - Version: " + pluginData.getVersion() + 
                         "\n - Provider: " + pluginData.getProvider() + 
                         "\n\n" + pluginData.getDescription();
        
        st.setText(pluginName + details);
        st.setBackground(white);
        st.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
       
        StyleRange boldStyle = new StyleRange();
        boldStyle.start = 0;
        boldStyle.length = pluginName.length();
        boldStyle.fontStyle = SWT.BOLD;
        st.setStyleRange(boldStyle);

        Composite rightContainer = new Composite(itemGroup, SWT.NONE);
        rightContainer.setBackground(white);
        rightContainer.setLayout(new GridLayout(1, false)); 
        GridData gdRight = new GridData(SWT.RIGHT, SWT.FILL, false, true);
        rightContainer.setLayoutData(gdRight);

        Composite statsTop = new Composite(rightContainer, SWT.NONE);
        statsTop.setBackground(white);
        statsTop.setLayout(new GridLayout(2, false)); 
        statsTop.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
        
        Label lblIcon = new Label(statsTop, SWT.NONE);
        lblIcon.setBackground(white);
        try {
            InputStream is = getClass().getResourceAsStream("/icons/download.png");
            if (is != null) {
                Image img = new Image(Display.getDefault(), is);
                lblIcon.setImage(img);
                lblIcon.addDisposeListener(e -> img.dispose());
            }
        } catch (Exception e) {}

        Label lblTotal = new Label(statsTop, SWT.NONE);
        lblTotal.setText(""+totalInstall);
        lblTotal.setBackground(white);

        Button btnInstall = new Button(rightContainer, SWT.PUSH);
        btnInstall.setText("Install");
        GridData gdBtn = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true);
        btnInstall.setLayoutData(gdBtn);
        
        btnInstall.addListener(SWT.Selection, e -> {
            System.out.println("Installing: " + pluginData.getName());
            chatView.addMessage(chatView.SYSTEM, "Installing: " + pluginData.getName(), null, chatView.NOT_INDEX);
            increment(pluginData, lblTotal); 
            PluginInstallerP2 pluginInstaller = new PluginInstallerP2();
            pluginInstaller.installPlugin(pluginData.getJarUrl(), pluginData.getId());
            installPlugin();
        });
    }
/* 
    private void createPluginItem(Composite parent, PluginData pluginData) {
        // Get system white color
        Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);

        // Create the group container
        Group itemGroup = new Group(parent, SWT.NONE);
        itemGroup.setBackground(white);
        itemGroup.setLayout(new GridLayout(2, false));
        itemGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Initialize StyledText
        StyledText st = new StyledText(itemGroup, SWT.WRAP | SWT.READ_ONLY);
        
        // Construct the text parts
        String pluginName = pluginData.getName();
        String details = "\n - Version: " + pluginData.getVersion() + 
                         "\n - Provider: " + pluginData.getProvider() + 
                         "\n\n" + pluginData.getDescription();
        
        st.setText(pluginName + details);
        st.setBackground(white);
        st.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Apply Bold style only to the plugin name
        StyleRange boldStyle = new StyleRange();
        boldStyle.start = 0;
        boldStyle.length = pluginName.length();
        boldStyle.fontStyle = SWT.BOLD;
        st.setStyleRange(boldStyle);

        // Create the Install button
        Button btnInstall = new Button(itemGroup, SWT.PUSH);
        btnInstall.setText("Install");
        
        btnInstall.addListener(SWT.Selection, e -> {
            System.out.println("Installing: " + pluginData.getName());
            PluginInstallerP2 pluginInstaller = new PluginInstallerP2();
            pluginInstaller.installPlugin(pluginData.getJarUrl(), pluginData.getId());
            installPlugin();
            increment(pluginData); 
        });
    }
*/
    public void installPlugin() {
    	String repoUrl = "";
        String featureId = "";

        Thread installThread = new Thread(() -> {
            P2Installer installer = new P2Installer();
            installer.installFromRepository(repoUrl, featureId);
        });
        
        installThread.start();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Close", true);
    }
   
    public void increment(PluginData data, Label lblTotal) {
        MongoDB mongoDB = getMongoDBPlugins();
        int total = 0;
        if(mongoDB != null){
            PluginRepository repository = new PluginRepository(mongoDB);
            System.out.println("\n--- Processing Plugin Install Count ---");
            PluginDocument myPlugin = repository.getPluginById(data.getId());
            if (myPlugin != null) {
            	
                int currentCount = myPlugin.getInstallCount();
                total = currentCount + 1;
                myPlugin.setInstallCount(total);
                myPlugin.setName(data.getName());
                myPlugin.setVersion(data.getVersion());
                myPlugin.setProvider(data.getProvider());
                myPlugin.setDescription(data.getDescription());
                System.out.println("Plugin found. Incrementing count to: " + myPlugin.getInstallCount());
            } else {
            	
                System.out.println("Plugin not found. Creating new entry with count = 1");
                myPlugin = new PluginDocument();
                myPlugin.setId(data.getId());
                myPlugin.setName(data.getName());
                myPlugin.setVersion(data.getVersion());
                myPlugin.setProvider(data.getProvider());
                myPlugin.setDescription(data.getDescription());
                total = 1;
                myPlugin.setInstallCount(total); // Empezamos en 1 porque se está instalando ahora mismo
            }
            repository.savePlugin(myPlugin);
            mongoDB.close();
            lblTotal.setText(""+total);
        }
    }
    
    public void db(PluginData data) {
        // 1. Setup the connection
    	IPreferenceStore store;
    	String mongoURI;
    	String mongoDatabase;
    	store = Activator.getDefault().getPreferenceStore();
    	mongoDatabase = caret.ChatView.MONGO_DATABASE;
    	mongoURI  = store.getString(PreferenceConstants.P_MONGO_URI);
		if (mongoURI != null && !mongoURI.isEmpty()  && mongoDatabase != null && !mongoDatabase.isEmpty()) {
			MongoDB mongoDB = new MongoDB();
	    	mongoDB.setDatabase(mongoDatabase);
	    	mongoDB.setConnectionURI(mongoURI);
	    	if(mongoDB.connect()) {
	    		mongoDB.setupCollection("plugins");
	 	        
	            PluginRepository repository = new PluginRepository(mongoDB);

	            System.out.println("\n--- Adding New Plugin ---");
	            PluginDocument myPlugin = new PluginDocument();
	            myPlugin.setId(data.getId());
	            myPlugin.setName(data.getName());
	            myPlugin.setVersion(data.getVersion());
	            myPlugin.setProvider(data.getProvider());
	            myPlugin.setDescription(data.getDescription());
	            myPlugin.setInstallCount(0);

	            repository.savePlugin(myPlugin);

	            System.out.println("\nProcess finished successfully.");
	    		
	 	        mongoDB.close();
	    	}
	       
		}
    }
    public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
	
	public MongoDB getMongoDBPlugins() {
	        IPreferenceStore store;
	        String mongoURI;
	        String mongoDatabase;
	        store = Activator.getDefault().getPreferenceStore();
	        mongoDatabase = caret.ChatView.MONGO_DATABASE;
	        mongoURI  = store.getString(PreferenceConstants.P_MONGO_URI);
	        
	        if (mongoURI != null && !mongoURI.isEmpty()  && mongoDatabase != null && !mongoDatabase.isEmpty()) {
	            MongoDB mongoDB = new MongoDB();
	            mongoDB.setDatabase(mongoDatabase);
	            mongoDB.setConnectionURI(mongoURI);
	            
	            if(mongoDB.connect()) {
	                mongoDB.setupCollection("plugins");
	                return mongoDB;
	            }
	        }
	        return null;
	}
	
	public int getPluginInstallCount(String id) {
		int total = 0;
		MongoDB mongoDB = getMongoDBPlugins();
        if(mongoDB != null){
            PluginRepository repository = new PluginRepository(mongoDB);
            System.out.println("\n--- getCount ---");
            total = repository.getTotalInstallCount(id);
            mongoDB.close();
        }
		return total;
	}
}