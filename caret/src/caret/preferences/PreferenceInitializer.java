package caret.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import com.google.gson.Gson;

import caret.Activator;
import caret.ChatView;
import caret.agent.AgentInterface;
import caret.tasks.ITasksGroup;
import caret.tasks.JavaConcept;
import caret.tasks.Parameter;
import caret.tasks.Task;
import caret.tasks.TasksGroup;
import caret.tasks.TasksManager;
import caret.validator.ValidatorInterface;

/**
 * Class used to initialize default preference values.
 */

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	
	public static final String CONTEXT_EXTENDED_CLASS = "Extended Class";
	public static final String CONTEXT_IMPLEMENTED_INTERFACE = "Implemented Interface";
	public static final String CONTEXT_METHOD_PARAMETERS = "Method parameters";
	public static final String CONTEXT_METHOD_VARIABLES = "Local method variables";
	public static final String CONTEXT_ATTRIBUTE = "Attribute";
	
	public void initializeDefaultPreferences() {
		System.out.println("## INITIALIZER CHATBOT ");
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PATH_SAVE, System.getProperty("user.home"));
		store.setDefault(PreferenceConstants.P_AGENT, "GPT");
		String listAgents = "";
		IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(ChatView.EXTENSION_POINT_AGENT);
		if(extensions.length>0) {
			List<String> taskClassifierAgents = new ArrayList<String>();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
				try {
					if(((AgentInterface) element.createExecutableExtension("class")).isLLM() == true ){
						AgentInterface agent = (AgentInterface) element.createExecutableExtension("class");
						taskClassifierAgents.add(element.getAttribute("name"));
						System.out.println("name plugin: "+element.getAttribute("name"));
					}
				} catch (Exception e) {
					System.out.println("ERROR PREFERENCEPAGE: "+e.getMessage());			
				}
			}
			listAgents = String.join(",", taskClassifierAgents);
		}
		store.setDefault(PreferenceConstants.P_LIST_TASK_CLASSIFIER_AGENTS, listAgents);
		store.setDefault(PreferenceConstants.P_LIST_CONTENT_ASSISTANT_AGENTS, listAgents);
		listAgents = "";
		if(extensions.length>0) {
			String[] taskClassifierAgents = new String[extensions.length];
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
				try {
					AgentInterface taskProcessingAgent = (AgentInterface) element.createExecutableExtension("class");
					taskClassifierAgents[i] = element.getAttribute("name");
					System.out.println("name plugin: "+element.getAttribute("name"));
				} catch (Exception e) {
					System.out.println("ERROR PREFERENCEPAGE: "+e.getMessage());			
				}
			}
			listAgents = String.join(",", taskClassifierAgents);
		}
		store.setDefault(PreferenceConstants.P_LIST_TASK_PROCESSING_AGENTS, listAgents);
		
		String listValidators = "";
		IConfigurationElement [] extensions2 = reg.getConfigurationElementsFor(ChatView.EXTENSION_POINT_VALIDATOR);
		List<String> validators = new ArrayList<String>();
		for (int i = 0; i < extensions2.length; i++) {
			IConfigurationElement element = extensions2[i];
			validators.add(element.getAttribute("id"));
		}
		listValidators = String.join(",", validators);
		store.setDefault(PreferenceConstants.P_LIST_VALIDATORS, listValidators);
		
		String strListTaskName = "";
		IConfigurationElement [] extensions3 = reg.getConfigurationElementsFor(ChatView.EXTENSION_POINT_TASKS);
		List<String> listTaskName = new ArrayList<String>();
		List<Task> listTasks = new ArrayList<Task>();
		for (int i = 0; i < extensions3.length; i++) {
			IConfigurationElement element = extensions3[i];
			try {
				IConfigurationElement[] taskElement = element.getChildren("task");
				if(taskElement != null && taskElement.length > 0) {
					TasksGroup tasksGroup =  TasksManager.getTasksGroup(element);
					for (Task task : tasksGroup.getTasks()) {
						listTaskName.add(task.getName());
						listTasks.add(task);
						System.out.println("TASK:"+task.getName());
					}
				}
				ITasksGroup iTasksGroup = (ITasksGroup) element.createExecutableExtension("class");
				if(iTasksGroup != null) {
					for (Task task : iTasksGroup.getTasks()) {
						listTaskName.add(task.getName());
						listTasks.add(task);
						System.out.println("TASK:"+task.getName());
					}
				}			
			} catch (Exception e) {
				System.out.println("ERROR TASK:"+e.getMessage());
			}		
			
			//tasks.add(element.getAttribute("id"));
		}
		strListTaskName = String.join(",", listTaskName);
		store.setDefault(PreferenceConstants.P_LIST_TASKS, strListTaskName);
		
		String JSON = generateTaskJson(validators, listTasks);
		store.setDefault(PreferenceConstants.P_TABLE_TASKS, JSON);
		
		store.setDefault(PreferenceConstants.P_CONTEXT_EXTENDED_CLASS, true);
		store.setDefault(PreferenceConstants.P_CONTEXT_IMPLEMENTED_INTERFACE, true);
		store.setDefault(PreferenceConstants.P_CONTEXT_ATTRIBUTE, true);
		store.setDefault(PreferenceConstants.P_CONTEXT_METHOD_PARAMETERS, true);
		store.setDefault(PreferenceConstants.P_CONTEXT_METHOD_VARIABLES, true);
	}
	
	private String generateTaskJson(List<String> validators, List<Task> tasks) {
        HashMap<String, Boolean> validatorMap = new HashMap<>();
        for (String validator : validators) {
            validatorMap.put(validator, true);
        }

        // Create a list of PTask objects
        List<PTask> taskList = new ArrayList<>();
        for (Task task : tasks) {
            HashMap<String, Boolean> contextMap = new HashMap<String, Boolean> ();
        	contextMap.put(CONTEXT_EXTENDED_CLASS, true);
        	contextMap.put(CONTEXT_IMPLEMENTED_INTERFACE, true);
        	contextMap.put(CONTEXT_ATTRIBUTE, true);
        	
        	Parameter parameter = task.getParameterByType(JavaConcept.METHOD.name());
        	if( parameter != null) {
        		if(parameter.isRequired()) {
                	contextMap.put(CONTEXT_METHOD_PARAMETERS, true);
                	contextMap.put(CONTEXT_METHOD_VARIABLES, true);
        		}
        	}
            PTask pTask = new PTask(true, task.getName(), new HashMap<>(validatorMap), contextMap );
            taskList.add(pTask);
        }

        // Serialize the task list to JSON
        Gson gson = new Gson();
        return gson.toJson(taskList);
    }

}
