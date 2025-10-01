package caret.tasks;

public interface ITasksGroup {

	public String getId();
	
	public String getName();
	
	public String getDescription();
	
	public Task [] getTasks();
	
	public Task getTask (String taskCode);
	
	public Parameter [] getParameters ();
	
	public void runTask(Task task,  int eventType);
	
}
