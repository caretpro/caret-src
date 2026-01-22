package caret.tasks;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import caret.ChatView;

public class TasksGroup implements ITasksGroup {
	
	private String id;
	private String name;
	private String description;
	private Task [] tasks;

	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	private ChatView chatView = ChatView.getInstance();
	IResource resource = null;
    IProject project = null;
    
    public TasksGroup(String id, String name, String description){
    	this.id = id;
    	this.name = name;
    	this.description = description;
    }
    
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setParameters(ArrayList<Parameter> parameters) {
		this.parameters = parameters;
	}
	
	public void setTasks(Task[] tasks) {
		this.tasks = tasks;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Task[] getTasks() {
		return tasks;
	}

	@Override
	public Parameter[] getParameters() {
		Parameter [] params = new Parameter [parameters.size()];
		for (int i=0; i < parameters.size();i++) {
			params[i] = parameters.get(i);
		}
		return params;
	}

	@Override
	public void runTask(Task task, int eventType) {
		System.out.println("##@runTask: "+task.getCode()+", eventType: "+eventType);
		chatView.processCode(task, eventType, true);
	}
	
	public Parameter getParameter(String name, boolean required, boolean hasSource ) {
		for (int i=0; i < parameters.size();i++) {
			if(parameters.get(i).getParameterType().getName() == name) {
				Parameter parameter = new Parameter(parameters.get(i).getName(), parameters.get(i).getDescription(), 
						parameters.get(i).getParameterType(), required, hasSource);
				return parameter;
			}
		}
		return null;
	}

	@Override
	public Task getTask(String taskCode) {
		for (Task task : tasks) {
			if(task.getCode().equals(taskCode)) {
				return task;
			}
		}
		return null;
	}

}
