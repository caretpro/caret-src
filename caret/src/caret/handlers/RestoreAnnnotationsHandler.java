
package caret.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

import caret.ChatView;
public class RestoreAnnnotationsHandler {

    @Execute
    public void execute(Shell shell){
        ChatView chatView = ChatView.getInstance();
        if (chatView != null) {
        	chatView.restoreAnnotations();
        } else {
            System.err.println("ChatView instance is null.");
        }
    }
}