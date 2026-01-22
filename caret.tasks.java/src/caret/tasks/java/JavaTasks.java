package caret.tasks.java;

import caret.tasks.ATasksGroup;
import caret.tasks.JavaConcept;
import caret.tasks.OtherParameter;
import caret.tasks.Parameter;
import caret.tasks.Task;

public class JavaTasks extends ATasksGroup {

	
	public JavaTasks() {
		super();
	}
	
	public void loadTasks() {
		Parameter [] parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false ), 
				getParameter(JavaConcept.METHOD.name(), true, true)};
		Task task0 = new Task(OPTIMISE_CODE, "Optimise code", "Optimise code of the method", parameters, BIND, null, true, true);
		addTask(task0);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task1 = new Task(IMPROVE_EFFICIENCY, "Improve efficiency", "Improve efficiency of the method", parameters, BIND, null, true, true);
		addTask(task1);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task2 = new Task(IMPROVE_READABILITY, "Improve readability", "Improve readability of the method", parameters, BIND, null, true, true);
		addTask(task2);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task3 = new Task(REDUCE_COMPLEXITY, "Reduce complexity", "Reduce complexity of the method", parameters, BIND, null, true, true);
		addTask(task3);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task4 = new Task(FIND_ERROR, "Find Error", "Find Error in the method and fix it", parameters, BIND, null, true, true);
		addTask(task4);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task5 = new Task(EXPLAIN_METHOD, "Explain java method", "Explain java method", parameters, BIND, null, true, true);
		addTask(task5);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task6 = new Task(COMMENT_LINES, "Comment line by line", "Comment lines", parameters, BIND, null, false, false);
		addTask(task6);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true) };
		Task task7 = new Task(RENAME_METHOD, "Rename java method", "Rename java method", parameters, BIND, null, true, true);
		addTask(task7);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.CLASS.name(), true, true)};
		Task task8 = new Task(GENERATE_JAVADOC_FILE, "Generate a javadoc from a class", "Generate a javadoc from a class", parameters, BIND, null, false, false);
		addTask(task8);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.CLASS.name(), false, false),
				getParameter(JavaConcept.METHOD.name(), true, true)};
		Task task9 = new Task(GENERATE_JAVADOC_METHOD, "Generate a javadoc from a method", "Generate a javadoc from a method", parameters, BIND, null, false, false);
		addTask(task9);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false),
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.INTERFACE.name(), true, true),
				getParameter(JavaConcept.CLASS.name(), true, false)};
		Task task10 = new Task(GENERATE_CLASS_FROM_INTERFACE, "Generate a java class from a interface", "Create a class that implements an interface. For this task do not create an interface because it exists, just create the class", parameters, BIND, null, true, true);
		addTask(task10);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.SUBCLASS.name(), true, false),
				getParameter(JavaConcept.CLASS.name(), true, true)};
		Task task11 = new Task(GENERATE_SUBCLASS_FROM_ABSTRACT, "Generate a java subclass from a abstract class", "Create a java subclass from an abstract class or extends an abstract class", parameters, BIND, null, true, true);
		addTask(task11);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false), 
				getParameter(JavaConcept.TESTCLASS.name(), true, false), 
				getParameter(JavaConcept.CLASS.name(), true, true)};
		Task task12 = new Task(GENERATE_TEST_FROM_CLASS, "Generate a java test class from a class", "Create a java test class from a class", parameters, BIND, null, true, true);
		addTask(task12);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false),
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), false, false), 
				getParameter(JavaConcept.METHOD.name(), true, true),
				getParameter(OtherParameter.DESCRIPTION, false, false)};
		Task task13 = new Task(CREATE_METHOD, "Generate a java method", "Generate a java method", parameters, BIND, null, true, true);
		addTask(task13);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), true, false)};
		Task task14 = new Task(CREATE_PROJECT, "Create a java project", "Create a java project", parameters, BIND, null, false, false);
		addTask(task14);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), true, false)};
		Task task15 = new Task(CREATE_PACKAGE, "Create a java package", "Create a java package", parameters, BIND, null, false, false);
		addTask(task15);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false), 
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.INTERFACE.name(), true, false),
				getParameter(OtherParameter.DESCRIPTION, false, false)};
		Task task16 = new Task(CREATE_INTERFACE, "Create a java interface", "Create a java interface", parameters, BIND, null, true, true);
		addTask(task16);
		
		parameters = new Parameter [] {
				getParameter(JavaConcept.PROJECT.name(), false, false),
				getParameter(JavaConcept.PACKAGE.name(), false, false),
				getParameter(JavaConcept.CLASS.name(), true, false),
				getParameter(OtherParameter.DESCRIPTION, false, false)};
		Task task17 = new Task(CREATE_CLASS, "Create a java class", "Create a java class. This defined task doesn't include cases for creating a subclass that extends an abstract class (GENERATE_SUBCLASS_FROM_ABSTRACT) or for creating classes that implement an interface (GENERATE_CLASS_FROM_INTERFACE)."
				+ " For other cases, this type of task applies.", parameters, BIND, null, true, true);
		addTask(task17);
	}
	
}
