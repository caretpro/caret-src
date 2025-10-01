package caret.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

import caret.ChatView;
public class ClearHandler {

    @Execute
    public void execute(Shell shell){
        ChatView chatView = ChatView.getInstance();
        if (chatView != null) {
            chatView.clearChatSession();
            //chatView.init();
        } else {
            System.err.println("ChatView instance is null.");
        }
    }
}