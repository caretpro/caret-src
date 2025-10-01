package caret.tasks.java;

import caret.ChatView;
import caret.data.Context;
import caret.data.Interaction;
import caret.project.Resource;
import caret.tasks.IDEBind;
import caret.tasks.ITasksGroup;
import caret.tasks.JavaConcept;
import caret.tasks.JavaParameter;
import caret.tasks.OtherParameter;
import caret.tasks.Parameter;
import caret.tasks.Task;
import caret.tool.Util;
import caret.ui.InputParametersDialog;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
public class JavaTasks implements ITasksGroup {

	private String id = "java";
	private String name = "java";
	private String description = "java tasks";
	private Task [] tasks = new Task [18];
	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	private ChatView chatView = ChatView.getInstance();
	IResource resource = null;
    IProject project = null;
    
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
    
	public JavaTasks() {
		parameters.add(new Parameter("projectName", "Project name", new JavaParameter(JavaConcept.PROJECT))); 
		parameters.add(new Parameter("packageName", "Package name", new JavaParameter(JavaConcept.PACKAGE)));
		parameters.add(new Parameter("interfaceName", "Interface name", new JavaParameter(JavaConcept.INTERFACE)));
		parameters.add(new Parameter("className", "Class name", new JavaParameter(JavaConcept.CLASS)));
		parameters.add(new Parameter("subclassName", "Sub class name", new JavaParameter(JavaConcept.SUBCLASS)));
		parameters.add(new Parameter("testclassName", "Test class name", new JavaParameter(JavaConcept.TESTCLASS)));
		parameters.add(new Parameter("methodName", "Method name", new JavaParameter(JavaConcept.METHOD)));
		parameters.add(new Parameter("description", "Description", new OtherParameter(OtherParameter.DESCRIPTION)));
		loadTasks();
	}
	
	public void loadTasks() {
		String bind = IDEBind.CONTEXTUAL.name();
		Parameter [] parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false ), 
				getParameter(JavaConcept.METHOD.name(), true, true)};
		Task task = new Task(OPTIMISE_CODE, "Optimise code", "Optimise code of the method. The requested task must explicitly mention the word 'method'", parameters, bind, null, true, true);
		tasks[0] = task;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task1 = new Task(IMPROVE_EFFICIENCY, "Improve efficiency", "Improve efficiency of the method. The requested task must explicitly mention the word 'method'", parameters, bind, null, true, true);
		tasks[1] = task1;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task2 = new Task(IMPROVE_READABILITY, "Improve readability", "Improve readability of the method. The requested task must explicitly mention the word 'method'", parameters, bind, null, true, true);
		tasks[2] = task2;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task3 = new Task(REDUCE_COMPLEXITY, "Reduce complexity", "Reduce complexity of the method. The requested task must explicitly mention the word 'method'", parameters, bind, null, true, true);
		tasks[3] = task3;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task4 = new Task(FIND_ERROR, "Find Error", "Find Error in the method and fix it. The requested task must explicitly mention the word 'method'", parameters, bind, null, true, true);
		tasks[4] = task4;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task5 = new Task(EXPLAIN_METHOD, "Explain java method", "Explain java method. The requested task must explicitly mention the word 'method'", parameters, bind, null, true, true);
		tasks[5] = task5;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task6 = new Task(COMMENT_LINES, "Comment line by line", "Comment lines", parameters, bind, null, false, false);
		tasks[6] = task6;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task7 = new Task(RENAME_METHOD, "Rename java method", "Rename java method", parameters, bind, null, true, true);
		tasks[7] = task7;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.CLASS.name(), true, true)};
		Task task8 = new Task(GENERATE_JAVADOC_FILE, "Generate a javadoc from a class", "Generate a javadoc from a class", parameters, bind, null, false, false);
		tasks[8] = task8;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.CLASS.name(), false, false),
				getParameter(JavaConcept.METHOD.name(), true, true)};
		Task task9 = new Task(GENERATE_JAVADOC_METHOD, "Generate a javadoc from a method", "Generate a javadoc from a method", parameters, bind, null, false, false);
		tasks[9] = task9;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false),
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.INTERFACE.name(), true, true),
				getParameter(JavaConcept.CLASS.name(), true, false)};
		Task task10 = new Task(GENERATE_CLASS_FROM_INTERFACE, "Generate a java class from a interface", "Create a class that implements an interface. For this task do not create an interface because it exists, just create the class", parameters, bind, null, true, true);
		tasks[10] = task10;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.SUBCLASS.name(), true, false),
				getParameter(JavaConcept.CLASS.name(), true, true)};
		Task task11 = new Task(GENERATE_SUBCLASS_FROM_ABSTRACT, "Generate a java subclass from a abstract class", "Create a java subclass from an abstract class or extends an abstract class", parameters, bind, null, true, true);
		tasks[11] = task11;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.TESTCLASS.name(), true, false), 
				getParameter(JavaConcept.CLASS.name(), true, true)};
		Task task12 = new Task(GENERATE_TEST_FROM_CLASS, "Generate a java test class from a class", "Create a java test class from a class", parameters, bind, null, true, true);
		tasks[12] = task12;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false),
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true),
				getParameter(OtherParameter.DESCRIPTION, false, false)};
		Task task13 = new Task(CREATE_METHOD, "Generate a java method", "Generate a java method", parameters, bind, null, true, true);
		tasks[13] = task13;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), true, false)};
		Task task14 = new Task(CREATE_PROJECT, "Create a java project", "Create a java project", parameters, bind, null, false, false);
		tasks[14] = task14;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), true, false)};
		Task task15 = new Task(CREATE_PACKAGE, "Create a java package", "Create a java package", parameters, bind, null, false, false);
		tasks[15] = task15;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.INTERFACE.name(), true, false),
				getParameter(OtherParameter.DESCRIPTION, false, false)};
		Task task16 = new Task(CREATE_INTERFACE, "Create a java interface", "Create a java interface", parameters, bind, null, true, true);
		tasks[16] = task16;
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false),
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), true, false),
				getParameter(OtherParameter.DESCRIPTION, false, false)};
		Task task17 = new Task(CREATE_CLASS, "Create a java class", "Create a java class. This defined task doesn't include cases for creating a subclass that extends an abstract class (GENERATE_SUBCLASS_FROM_ABSTRACT) or for creating classes that implement an interface (GENERATE_CLASS_FROM_INTERFACE)."
				+ " For other cases, this type of task applies.", parameters, bind, null, true, true);
		tasks[17] = task17;
	}

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
	public Task[] getTasks() {	
		return tasks;
	}

	@Override
	public Parameter [] getParameters() {
		Parameter [] params = new Parameter [parameters.size()];
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
		System.out.println("######getITask (JAVA) - ENTER");
		for (Task task : tasks) {
			if(task != null) {
				if(task.getCode().equals(taskCode)) {
					System.out.println("######getITask - IGUAL:"+task.getCode()+"-"+taskCode);
					return task;
				}else {
					System.out.println("######getITask - DIFERENTE:"+task.getCode()+"-"+taskCode);
				}
			}
		}
		System.out.println("######getITask - RETURN NULL");
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
			break;
		}
		
	}
	
}
