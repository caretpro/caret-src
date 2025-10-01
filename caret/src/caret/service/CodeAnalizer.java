package caret.service;

import org.eclipse.core.resources.IResource;

import com.google.gson.Gson;

import caret.ChatView;
import caret.agent.Response;
import caret.agent.ResponseJSON;
import caret.project.Resource;
import caret.tool.Util;

public class CodeAnalizer {
	ChatView chatView = ChatView.getInstance();
	IResource iResource = Resource.getSelectedResource();
	Resource resource = null;
	String source = null;
	public CodeAnalizer(IResource iResource){
		resource = new Resource (iResource);
		source = resource.getSource();
	}
	
	public void analize() {
		String prompt="You are a code assistant that helps software developers in programming tasks. You just outputs JSON response with an array with elements of the four parameters: methodName, requiredModification (true or false), modifiedMethod and explanation). Analize method by method the following Java code (if you find a method not implemented or not optimised or with errors, put the put the new modified full method to solve it in the parameter modifiedMethod, otherwise put NO_MODIFICATION): "
				+ "\n"+source;
		Response response = chatView.getAgent(true).processMessage(Util.codeToLine(prompt, true), chatView.TEMPERATURE_HIGH);
		if(response != null) {
    		String json = Util.getJSON(response.getText());
    		ResponseModification [] responseModification = null;
    		if(json!=null) {
    			Gson gson = new Gson();
    			try {
    				responseModification = gson.fromJson(json, ResponseModification[].class);
            		System.out.println("ResponseModification: "+responseModification[0].getModifiedMethod());
				} catch (Exception e) {
					System.out.println("Error processing JSON");
				}
    		}
    	}
	}
	
}
