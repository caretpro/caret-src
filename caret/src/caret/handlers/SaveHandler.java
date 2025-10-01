package caret.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import caret.Activator;
import caret.ChatView;
import caret.preferences.PreferenceConstants;
import caret.tool.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class SaveHandler extends AbstractHandler{
	
	ChatView chatView = ChatView.getInstance();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getCurrent().getActiveShell();
		ChatView chatView = ChatView.getInstance();
		boolean result = MessageDialog.openConfirm(shell, "Save", "Do you want to save the chat session?");
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String pathPreference =store.getString(PreferenceConstants.P_PATH_SAVE)+System.lineSeparator();
		if (result){
			String path = pathPreference+"chatsession-"+Util.getDateFormat("yyyyMMdd-HHmmss")+".txt";
			if (saveSession(path, chatView.getChatSession())) {
				MessageDialog.openInformation(shell, "Saved chat session", "The chat session has been saved successfully.\n File: "+path);
			}else {
				MessageDialog.openError(shell, "Error", "The chat session could not be saved");
			}			
		}
		System.out.println("Save file");
		return null;
	}
	
	public boolean saveSession(String path, String content) {
		 try {
	            File file = new File(path);
	            if (!file.exists()) {
	                file.createNewFile();
	            }else {
	            	return false;
	            }
	            FileWriter fw = new FileWriter(file);
	            BufferedWriter bw = new BufferedWriter(fw);
	            bw.write(content);
	            bw.close();
	            System.out.println("SAVED FILE");
	            return true;
	        } catch (Exception e) {
	            System.out.println("ERROR SAVING FILE: "+e.getMessage());
	            return false;
	        }
	}	

}
