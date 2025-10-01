package caret.handlers;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import caret.ChatView;
import caret.project.Resource;
import caret.tasks.IDEBind;
import caret.tasks.ITasksGroup;
import caret.tasks.JavaConcept;
import caret.tasks.Task;
import caret.tasks.TasksGroup;
import caret.tasks.TasksManager;

public class DefineCommands extends ExtensionContributionFactory {

	public static final String TASK_HANDLER ="caret.commands.Task";
	ChatView chatView = ChatView.getInstance();
	
    @Override
    public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
    	System.out.println("#DefineCommands 1");
    	List<Task> tasks = TasksManager.getPreferenceTasks();
    	createMenu(tasks, serviceLocator, additions);
    	System.out.println("#DefineCommands 2");
    }
    
    private void createMenu(List<Task> tasks, IServiceLocator serviceLocator, IContributionRoot additions) {
    	System.out.println("#DefineCommands 3");
        IResource iResource = Resource.getSelectedResource();
        if(iResource != null) {
        	for (Task task : tasks) {
    			if(task.getBind().equals(IDEBind.CONTEXTUAL.name())) {
    	        	if(Resource.isIStructuredSelection()) {
    					if(iResource.getType() == IResource.FILE) {
    						Resource resource = new Resource(iResource);
    						if(task.parameterWithSource() && resource.isICompilationUnit()){
    							if(resource.isClass() && task.getParameterByType(JavaConcept.CLASS.name()) != null) {
    								if(task.getParameterByType(JavaConcept.CLASS.name()).hasSource()) {
    									addMenuItem(serviceLocator, additions, task);
    								}
    							}
    							if(resource.isInterface() && task.getParameterByType(JavaConcept.INTERFACE.name()) != null) {
    								if(task.getParameterByType(JavaConcept.INTERFACE.name()).hasSource()) {
    									addMenuItem(serviceLocator, additions, task);
    								}
    							}
    						}
    					}
    					if(iResource.getType() == IResource.FOLDER) {
    						if(!task.parameterWithSource()){
    							addMenuItem(serviceLocator, additions, task);
    						}
    						
    					}
    				}
    				if(Resource.isITextSelection() && (new Resource(iResource)).isICompilationUnit()) {
    					if(task.getParameterByType(JavaConcept.METHOD.name()) != null) {
    						if(task.getParameterByType(JavaConcept.METHOD.name()).hasSource()) {
    					        addMenuItem(serviceLocator, additions, task);
    						}
    					}
    				}
    			}
    		}
        }else {
        	System.out.println("#iResource = NULL, a IResource hasn't been selected");
        }
    	System.out.println("#DefineCommands 4");
    }
    
    private void addMenuItem(IServiceLocator serviceLocator, IContributionRoot additions, Task task) {
    	HashMap<String, String> params = new HashMap<String, String>();	
		params.put("iTasksGroupId", task.getiTasksGroupId());
        params.put("taskCode", task.getCode());
		CommandContributionItemParameter itemParameter;
		if(task.getCommandId() == null || task.getCommandId() == "") {
			itemParameter = new CommandContributionItemParameter(serviceLocator, task.getCode(), TASK_HANDLER, SWT.PUSH);
		}else {
			itemParameter = new CommandContributionItemParameter(serviceLocator, task.getCode(), task.getCommandId(), SWT.PUSH);
		}
        itemParameter.label = task.getName();
        itemParameter.parameters = params;
        CommandContributionItem item = new CommandContributionItem(itemParameter);
        item.setVisible(true);
        additions.addContributionItem(item, null);
    }

}