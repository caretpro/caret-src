package caret.tasks;

public interface ITasksGroup {

	public String getId();
	
	public String getName();
	
	public String getDescription();
	
	public void setId(String id);
	
	public void setName(String name);
	
	public void setDescription(String description);
	
	public Task [] getTasks();
	
	public Task getTask (String taskCode);
	
	public Parameter [] getParameters ();
	
	public void runTask(Task task,  int eventType);
	
}
