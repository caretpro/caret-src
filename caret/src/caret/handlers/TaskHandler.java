package caret.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;

import caret.ChatView;
import caret.project.Resource;
import caret.tasks.ITasksGroup;
import caret.tasks.JavaConcept;
import caret.tasks.Parameter;
import caret.tasks.Task;
import caret.tasks.TasksManager;
import caret.tool.Parser;

public class TaskHandler extends AbstractHandler{
	
	ChatView chatView = ChatView.getInstance();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
    	System.out.println("###EVENT toString:"+event.toString());
    	System.out.println("###EVENT ITG:"+event.getParameters().get("iTasksGroupId"));
    	System.out.println("###EVENT TC:"+event.getParameters().get("taskCode"));
    	ITasksGroup iTasksGroup = TasksManager.getITasksGroup(event.getParameters().get("iTasksGroupId").toString());
    	if(iTasksGroup != null) {
    		System.out.println("--EVENT iTG:"+iTasksGroup.getName());
    		Task task = iTasksGroup.getTask(event.getParameters().get("taskCode").toString());
    		if(task != null) {
    			System.out.println("--EVENT Task:"+task.getCode());
    			IResource iResource = Resource.getSelectedResource();
    			if(Resource.isIStructuredSelection()) {
    				System.out.println("TASK HANDLER: 1 ->");
    				if(iResource.getType() == IResource.FILE) {
    					System.out.println("TASK HANDLER: 2 ->");
    					Resource resource = new Resource(iResource);
    					if(task.parameterWithSource() || resource.isICompilationUnit()){
    						System.out.println("TASK HANDLER: 3 ->");
							if(task.getParameterByType(JavaConcept.CLASS.name())!= null && resource.isClass()){
								System.out.println("TASK HANDLER: CLASS1 ->");
								Parameter parameter = task.getParameterByType(JavaConcept.CLASS.name());
								if(parameter.hasSource()) {
									System.out.println("TASK HANDLER: CLASS2 ->"+ Parser.getClassName(iResource.getName(), Parser.TYPE_FILENAME));
									parameter.setValue(Parser.getClassName(iResource.getName(), Parser.TYPE_FILENAME));
									parameter.setSource(resource.getSource());
								}
							}
							if(task.getParameterByType(JavaConcept.INTERFACE.name())!=null && resource.isInterface()){
								System.out.println("TASK HANDLER: INTERFACE1 ->");
								Parameter parameter = task.getParameterByType(JavaConcept.INTERFACE.name());
								if(parameter.hasSource()) {
									System.out.println("TASK HANDLER: INTERFACE2 ->"+ Parser.getClassName(iResource.getName(), Parser.TYPE_FILENAME));
									parameter.setValue(Parser.getClassName(iResource.getName(), Parser.TYPE_FILENAME));
									parameter.setSource(resource.getSource());
								}
							}
    					}
    				}
    			}
    			chatView.currentEventType = chatView.EVENT_TYPE_MENU;
    			iTasksGroup.runTask(task, ChatView.EVENT_TYPE_MENU);
    		}else {
    			System.out.println("--EVENT Task NULL");
    		}
    		
    	}   	
		return null;
	}

}
