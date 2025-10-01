package caret.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import caret.ChatView;

public class TestHandler extends AbstractHandler{
	
	ChatView chatView = ChatView.getInstance();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ChatView chatView = ChatView.getInstance();
		//chatView.evaluation();
		return null;
	}

}


