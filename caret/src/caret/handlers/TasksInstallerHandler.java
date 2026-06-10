package caret.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import caret.ChatView;
import caret.RemoteP2Reader;
import caret.RemoteP2ReaderT;
import caret.ui.TasksPluginDialog;
public class TasksInstallerHandler {

    @Execute
    public void execute(Shell shell){
        ChatView chatView = ChatView.getInstance();
        if (chatView != null) {
        	 try {
                 //new RemoteP2ReaderT().scan();
        		 TasksPluginDialog responseDialog = new TasksPluginDialog(Display.getCurrent().getActiveShell());
			     int option = responseDialog.open();
             } catch (Exception e) {
            	 chatView.addMessage(chatView.BOT,"Error connection to the tasks repository", null, chatView.NOT_INDEX);
                 e.printStackTrace();
                 
             }
        } else {
       	 chatView.addMessage(chatView.BOT,"Failed to connect to the tasks repository", null, chatView.NOT_INDEX);
            System.err.println("ChatView instance is null.");
        }
    }
}