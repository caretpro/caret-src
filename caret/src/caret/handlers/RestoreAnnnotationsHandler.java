
package caret.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

import caret.ChatView;
import caret.RemoteP2Reader;
public class RestoreAnnnotationsHandler {

    @Execute
    public void execute(Shell shell){
        ChatView chatView = ChatView.getInstance();
        if (chatView != null) {
        	 try {
        		 chatView.restoreAnnotations();
             } catch (Exception e) {
                 chatView.addMessage(chatView.BOT,"Error restoring annotations", null, chatView.NOT_INDEX);
                 e.printStackTrace();
             }
        } else {
        	chatView.addMessage(chatView.BOT,"Failed to restore annotations", null, chatView.NOT_INDEX);
        }
    }
}