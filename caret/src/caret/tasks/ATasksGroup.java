package caret.tasks;

import caret.ChatView;
import caret.tool.Log;

import java.util.ArrayList;

public abstract class ATasksGroup implements ITasksGroup {

	private String id;
	private String name;
	private String description;
	private ArrayList<Task> tasks = new ArrayList<Task>();
	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	private ChatView chatView = ChatView.getInstance();
    
    public static final String OPTIMISE_CODE = "OPTIMISE_CODE";
    public static final String IMPROVE_EFFICIENCY = "IMPROVE_EFFICIENCY";
    public static final String IMPROVE_READABILITY = "IMPROVE_READABILITY";
    public static final String REDUCE_COMPLEXITY = "REDUCE_COMPLEXITY";
    public static final String FIND_ERROR = "FIND_ERROR";
    public static final String EXPLAIN_METHOD = "EXPLAIN_METHOD";
    public static final String COMMENT_LINES = "COMMENT_LINES";
    public static final String RENAME_METHOD = "RENAME_METHOD";
    public static final String GENERATE_JAVADOC_FILE = "GENERATE_JAVADOC_FILE";
    public static final String GENERATE_JAVADOC_METHOD = "GENERATE_JAVADOC_METHOD";
    public static final String GENERATE_CLASS_FROM_INTERFACE = "GENERATE_CLASS_FROM_INTERFACE";
    public static final String GENERATE_SUBCLASS_FROM_ABSTRACT = "GENERATE_SUBCLASS_FROM_ABSTRACT";
    public static final String GENERATE_TEST_FROM_CLASS = "GENERATE_TEST_FROM_CLASS";
    public static final String CREATE_METHOD = "CREATE_METHOD";
    public static final String CREATE_PROJECT = "CREATE_PROJECT";
    public static final String CREATE_PACKAGE = "CREATE_PACKAGE";
    public static final String CREATE_CLASS = "CREATE_CLASS";
    public static final String CREATE_INTERFACE = "CREATE_INTERFACE";
    
    public static final String BIND = IDEBind.CONTEXTUAL.name();
    
	public ATasksGroup() {
		loadParameters();
		loadTasks();
	}
	
	public void loadParameters() {
		parameters.add(new Parameter("projectName", "Project name", new JavaParameter(JavaConcept.PROJECT))); 
		parameters.add(new Parameter("packageName", "Package name", new JavaParameter(JavaConcept.PACKAGE)));
		parameters.add(new Parameter("interfaceName", "Interface name", new JavaParameter(JavaConcept.INTERFACE)));
		parameters.add(new Parameter("className", "Class name", new JavaParameter(JavaConcept.CLASS)));
		parameters.add(new Parameter("subclassName", "Sub class name", new JavaParameter(JavaConcept.SUBCLASS)));
		parameters.add(new Parameter("testclassName", "Test class name", new JavaParameter(JavaConcept.TESTCLASS)));
		parameters.add(new Parameter("methodName", "Method name", new JavaParameter(JavaConcept.METHOD)));
		parameters.add(new Parameter("description", "Description", new OtherParameter(OtherParameter.DESCRIPTION)));
		loadNewParameters();
	}
	
	public void loadNewParameters() {
		
	}
	
	public abstract void loadTasks();
	

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setName(String name) {
	    this.name = name;
	}

	@Override
	public void setDescription( String description) {
		this.description = description;
	}
	
	@Override
	public Task[] getTasks() {
		return tasks.toArray(new Task[0]);
	}
	
	public void addTask(Task task) {
		tasks.add(task);
	}

	@Override
	public Parameter [] getParameters() {
		Parameter [] params = new Parameter [parameters.size()];
		Log.d("Params length -> "+params.length);
		for (int i=0; i < parameters.size();i++) {
			params[i] = parameters.get(i);
		}
		return params;
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
			if(task != null) {
				if(task.getCode().equals(taskCode)) {
					return task;
				}
			}
		}
		return null;
	}

	@Override
	public void runTask(Task task, int eventType) {
		System.out.println("#@runTask: "+task.getCode()+", eventType: "+eventType);
		switch (task.getCode()) {
		case OPTIMISE_CODE:
			chatView.processCode(task, eventType, true);
			break;
		case IMPROVE_EFFICIENCY:
			chatView.processCode(task, eventType, true);
			break;
		case IMPROVE_READABILITY:
			chatView.processCode(task, eventType, true);
			break;
		case REDUCE_COMPLEXITY:
			chatView.processCode(task, eventType, true);
			break;
		case FIND_ERROR:
			chatView.processCode(task, eventType, true);
			break;
		case EXPLAIN_METHOD:
			chatView.processCode(task, eventType, false);
			break;
		case COMMENT_LINES:
			chatView.processCode(task, eventType, true);
			break;
		case RENAME_METHOD:
			chatView.processCode(task, eventType, true);
			break;
		case GENERATE_JAVADOC_METHOD:
			chatView.processCode(task, eventType, true);
			break;
		case GENERATE_JAVADOC_FILE:
			chatView.createClass(task);
			break;
		case CREATE_PROJECT:
			chatView.createProject(task);
			break;
		case CREATE_PACKAGE:
			chatView.createPackage(task);
			break;
		case CREATE_INTERFACE:
			chatView.createClass(task);
			break;
		case CREATE_CLASS:
			chatView.createClass(task);
			break;
		case CREATE_METHOD:
			chatView.createMethod(task, eventType, false);
			break;
		case GENERATE_CLASS_FROM_INTERFACE:
			chatView.createClass(task);
			break;
		case GENERATE_SUBCLASS_FROM_ABSTRACT:
			chatView.createClass(task);
			break;
		case GENERATE_TEST_FROM_CLASS:
			chatView.createClass(task);
			break;
		default:
			chatView.processCode(task, eventType, true);
			break;
		}
		
	}
	
}
