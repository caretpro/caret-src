package caret.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;

import caret.Activator;
import caret.ChatView;
import caret.preferences.PTask;
import caret.preferences.PreferenceConstants;

public class TasksManager {
	
	static ChatView chatView = ChatView.getInstance();
	
	public static List<Task> getAllTasks(){
		List<Task> listTasks = new ArrayList<Task>();
		IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(ChatView.EXTENSION_POINT_TASKS);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				IConfigurationElement[] taskElement = element.getChildren("task");
				if(taskElement != null && taskElement.length > 0) {
					TasksGroup tasksGroup = getTasksGroup(element);
					if(tasksGroup != null) {
						if(tasksGroup.getTasks() != null) {
							updateITasksGroupId(tasksGroup, element.getAttribute("id"));
							Collections.addAll(listTasks, tasksGroup.getTasks());
						}
					}
					
				}
				
				ITasksGroup iTasksGroup = null;
				try {
					iTasksGroup = (ITasksGroup) element.createExecutableExtension("class");
				} catch (Exception e) {
					//System.out.println("createContributionItems-> NO INSTANCE iTasksGroup: "+e.getMessage());
				}
				if(iTasksGroup != null) {
					updateITasksGroupId(iTasksGroup, element.getAttribute("id"));
					Collections.addAll(listTasks, iTasksGroup.getTasks());
				}
			} catch (Exception e) {
				System.out.println("createContributionItems ("+element.getAttribute("name")+")-> ERROR TASK: "+e.getMessage());
			}			
		}
		
		return listTasks;
		
	}
	
	public static List<Task> getPreferenceTasks(){
		List<Task> listTasks = new ArrayList<Task>();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String json = store.getString(PreferenceConstants.P_TABLE_TASKS);
        if (json != null && !json.isEmpty()) {
            Gson gson = new Gson();
            PTask[] pTasks = gson.fromJson(json, PTask[].class);
            for (PTask pTask : pTasks) {
            	if(pTask.isEnabled()) {
	            	Task task = findTask(pTask.getTaskName());
	    			if( task != null) {
	    				listTasks.add(task);
	    			}
            	}
            }
        }
		return listTasks;
	}
	
	public static Task findTask(String taskName){
		List<Task> listTasks = getAllTasks();
		for (Task task : listTasks) {
			if(task.getName().equals(taskName)) {
        		return task;
        	}
		}
		return null;
	}

	private static void updateITasksGroupId(ITasksGroup tasksGroup, String idITaskGroupId) {
		for (Task task : tasksGroup.getTasks()) {
			task.setiTaskGroupId(idITaskGroupId);
		}
	}
	
	public static ITasksGroup findITasksGroup(String taskCode) {
		ITasksGroup iTasksGroup = null;
    	IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(ChatView.EXTENSION_POINT_TASKS);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
					iTasksGroup = getITasksGroup(element);
					for (Task task : iTasksGroup.getTasks()) {
						if(task.getCode() == taskCode) {
							return iTasksGroup;
						}
					}
			
			} catch (Exception e) {
				System.out.println("ERROR findITasksGroup:"+e.getMessage());
			}			
		}
		return iTasksGroup;
    }
	
	public static Task getTask(String taskCode) {
		System.out.println("#getTask taskCode:"+taskCode);
    	IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(ChatView.EXTENSION_POINT_TASKS);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				ITasksGroup iTasksGroup = getITasksGroup(element);
					for (Task task : iTasksGroup.getTasks()) {
						if(task.getCode().equals(taskCode)) {
							return task;
						}
					}
			} catch (Exception e) {
				System.out.println("ERROR getTask:"+e.getMessage());
			}			
		}
		return null;
    }
	
	public static ITasksGroup getITasksGroup(IConfigurationElement element) {
		ITasksGroup iTasksGroup = null;
		try {
			iTasksGroup = (ITasksGroup) element.createExecutableExtension("class");
			if(iTasksGroup != null) {
				return iTasksGroup;
			}
		} catch (Exception e) {
			//System.out.println("##ERROR getITasksGroup CLASS:"+element.getAttribute("id")+":"+e.getMessage());
		}	
		IConfigurationElement[] taskElement = element.getChildren("task");
		if(taskElement != null && taskElement.length > 0) {
			iTasksGroup = getTasksGroup(element);
		}
		return iTasksGroup;
	}
	
	public static ITasksGroup getITasksGroup(String id) {
    	IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(chatView.EXTENSION_POINT_TASKS);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			if(element.getAttribute("id").equals(id)) {
				ITasksGroup iTasksGroup = getITasksGroup(element);
				if(iTasksGroup != null) {
					return iTasksGroup;
				}else {
					System.out.println("getITasksGroup NULL:"+i);
				}
			}								
		}
		return null;
    }
	
	public static TasksGroup getTasksGroup(IConfigurationElement element) {
		TasksGroup tasksGroup = new TasksGroup(element.getAttribute("id"),element.getAttribute("name"),element.getAttribute("description"));
		IConfigurationElement[] taskElements = element.getChildren("task");
		Task [] tasks = new Task[taskElements.length];
		for (int i = 0; i < taskElements.length; i++) {
			tasks[i] = getTask(taskElements[i]);
		}
		tasksGroup.setTasks(tasks);
		return tasksGroup;
	}
	
	public static Task getTask(IConfigurationElement element) {
		Task task = new Task (element.getAttribute("code"),element.getAttribute("name"),element.getAttribute("description"),
				element.getAttribute("bind"),element.getAttribute("commandId"));
		if(element.getAttribute("instructions")!=null) {
			task.setInstructions(element.getAttribute("instructions"));
		}
		task.setPreviousValidation(Boolean.parseBoolean(element.getAttribute("previousValidation")));
		task.setPostValidation(Boolean.parseBoolean(element.getAttribute("postValidation")));
		IConfigurationElement[] parameterElements = element.getChildren("parameter");
		Parameter [] parameters = new Parameter [parameterElements.length];
		for (int i = 0; i < parameterElements.length; i++) {
			parameters[i] = getParameter(parameterElements[i]);
		}
		task.setParameters(parameters);
		return task;
	}
	
	public static Parameter getParameter(IConfigurationElement element) {
		Parameter parameter = new Parameter(element.getAttribute("name"), element.getAttribute("description"), new JavaParameter(element.getAttribute("parameterType")));
		parameter.setRequired(Boolean.valueOf(element.getAttribute("required")));	
		parameter.setHasSource(Boolean.valueOf(element.getAttribute("hasSource")));
		parameter.setValue(element.getAttribute("value"));
		return parameter;
	}
}

