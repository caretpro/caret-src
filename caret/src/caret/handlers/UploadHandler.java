package caret.handlers;

import java.io.InputStream;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import caret.Activator;
import caret.ChatView;
import caret.ZIPUploader;
import caret.preferences.PreferenceConstants;
import caret.tool.Util;

public class UploadHandler extends AbstractHandler {
	
	ChatView chatView = ChatView.getInstance();
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	ChatView chatView = ChatView.getInstance();
        IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
        IProject project = (IProject) ((IAdaptable) selection.getFirstElement()).getAdapter(IProject.class);
        
        try {
            IFile siteFile = project.getFile("site.xml");
            if (!siteFile.exists()) {
                return null;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            try (InputStream is = siteFile.getContents()) {
                Document doc = builder.parse(is);
                
                NodeList featureList = doc.getElementsByTagName("feature");
                if (featureList.getLength() > 0) {
                    Element featureElement = (Element) featureList.item(0);
                    
                    String extId = featureElement.getAttribute("id");           
                    String extVersion = featureElement.getAttribute("version"); 
                    
                    System.out.println("Target Extension ID: " + extId);
                    System.out.println("Target Extension Version: " + extVersion);
                    IPreferenceStore store;
                    store = Activator.getDefault().getPreferenceStore();
                    String uploadURL = store.getString(PreferenceConstants.P_TASKS_UPLOAD_URL);
                    String bearerToken = store.getString(PreferenceConstants.P_TASKS_UPLOAD_TOKEN);
                    String p2URL = store.getString(PreferenceConstants.P_TASKS_P2_REPOSITORY_URL);
                    System.out.println("uploadURL: " + uploadURL);
                    System.out.println("uploadToken: " + bearerToken);
                    System.out.println("p2URL: " + p2URL);
                    if(uploadURL==null || uploadURL.trim().isEmpty()){
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Tasks repository", "Missing task repository connection parameters");
                        System.out.println("uploadURL: EMPTY ");
                        return null;
                    }else{
                    	System.out.println("uploadURL: OK ");
                    }
                    try {
                    	chatView.addMessage(chatView.SYSTEM,"Uploading task plugin...", null, chatView.NOT_INDEX);
                        Path zipPath = ZIPUploader.zipFolder(project.getLocation().toPath());
                        ZIPUploader.uploadZipFile(uploadURL, bearerToken, zipPath, extId, extVersion );
                        chatView.addMessage(chatView.SYSTEM, Util.getBaseName(extId)+" has been successfully uploaded to the repository.", null, chatView.NOT_INDEX);
                    } catch (Exception e) {
                        System.err.println("Process failed: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Log error to Eclipse Error Log
            e.printStackTrace();
        }
        return null;
    }


}