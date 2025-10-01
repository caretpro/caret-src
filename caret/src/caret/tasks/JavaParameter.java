package caret.tasks;


public class JavaParameter extends ParameterType{
	
	JavaConcept javaConcept;
	
	public JavaParameter(JavaConcept javaConcept) {
		super(javaConcept.name());
	}
	
	public JavaParameter(String javaConcept) {
		super(javaConcept);
	}
	
}