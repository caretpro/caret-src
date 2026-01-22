package caret.tasks;

public class Task {

	private String code;
	private String name;
	private String description;
	private String instructions;
	private Parameter [] parameters;
	private String bind;
	private String commandId;
	private String iTasksGroupId = null;
	private boolean previousValidation = false;
	private boolean postValidation = false;
	
	public Task() {
		
	}
	
	public Task(String code, String name, String description, Parameter[] parameters, String bind, String handler, boolean previousValidation, boolean postValidation) {
		this(code, name, description, parameters, bind, handler);
		this.previousValidation = previousValidation;
		this.postValidation = postValidation;
	}

	public Task(String code, String name, String description, Parameter[] parameters, String bind, String handler) {
		this.code = code;
		this.name = name;
		this.description = description;
		this.parameters = parameters;
		this.bind = bind;
		this.commandId = handler;
	}
	
	public Task(String code, String name, String description, String instructions, Parameter[] parameters, String bind, String handler) {
		this.code = code;
		this.name = name;
		this.description = description;
		this.instructions = instructions;
		this.parameters = parameters;
		this.bind = bind;
		this.commandId = handler;
	}
	
	public Task(String code, String name, String description, String bind, String handler) {
		this.code = code;
		this.name = name;
		this.description = description;
		this.bind = bind;
		this.commandId = handler;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Parameter[] getParameters() {
		return parameters;
	}
	
	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
	}

	public String getBind() {
		return bind;
	}

	public void setBind(String bind) {
		this.bind = bind;
	}
	
	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String handler) {
		this.commandId = handler;
	}
	
	public Parameter getParameter(String parameterName) {
		for (Parameter parameter : parameters) {
			if(parameter.getName().equals(parameterName)) {
				return parameter;
			}
		}
		return null;
	}
	
	public Parameter getParameterByValue(String parameterValue) {
		for (Parameter parameter : parameters) {
			if(parameter.getValue()!=null) {
				if(parameter.getValue().equals(parameterValue)) {
					return parameter;
				}
			}
		}
		return null;
	}
	
	public Parameter getParameterByType(String parameterType) {
		for (Parameter parameter : parameters) {
			//System.out.println(parameter.getParameterType().getName()+"="+parameterType);
			if(parameter.getParameterType().getName().equals(parameterType)) {
				return parameter;
			}
		}
		return null;
	}
	
	public boolean parameterWithSource() {
		for (Parameter parameter : parameters) {
			if(parameter.hasSource()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAllParameterValue(Boolean OnlyRequired) {
		for (Parameter parameter : parameters) {
			if(OnlyRequired) {
				if(parameter.isRequired() && parameter.getValue() == null) {
					return false;
				}
			}else {
				if(parameter.getValue() == null) {
					return false;
				}
			}
		}
		return true;
	}
	
	public Parameter getParameterWithValue(String parameterType) {
		for (Parameter parameter : parameters) {
			if(parameter.getParameterType().getName().equals(parameterType)) {
				if(parameter.getValue() != null) {
					return parameter;
				}
			}
		}
		return null;
	}
	
	public String getDescriptionWithParameter() {
		String description = getDescription();
		if(getParameterWithValue(JavaConcept.TESTCLASS.name()) != null) {
			description = description.replace(" test class", " testclass");
			description = description.replace(" sub class", " subclass");
		}
		if(getParameterWithValue(JavaConcept.CLASS.name()) != null) {
			description = description.replace(" a class", " the class");
			description = description.replace(" a abstract class", " the abstract class");
			description = description.replace(" a parent class", " the parent class");
			description = description.replace(" class", " class "+getParameterWithValue(JavaConcept.CLASS.name()).getValue()+" ");
		}
		if(getParameterWithValue(JavaConcept.INTERFACE.name()) != null) {
			description = description.replace(" a interface", " the interface");
			description = description.replace(" interface", " interface "+getParameterWithValue(JavaConcept.INTERFACE.name()).getValue()+" ");
		}
		if(getParameterWithValue(JavaConcept.SUBCLASS.name()) != null) {
			description = description.replace(" a subclass", " the subclass");
			description = description.replace(" subclass", " subclass "+getParameterWithValue(JavaConcept.SUBCLASS.name()).getValue()+" ");
		}
		if(getParameterWithValue(JavaConcept.TESTCLASS.name()) != null) {
			description = description.replace(" a testclass", " the test class");
			description = description.replace(" testclass", " test class "+getParameterWithValue(JavaConcept.TESTCLASS.name()).getValue()+" ");
		}
		return description;
	}
	
	public String getiTasksGroupId() {
		return iTasksGroupId;
	}

	public void setiTaskGroupId(String iTaskGroupId) {
		this.iTasksGroupId = iTaskGroupId;
	}
	
	public boolean hasPreviousValidation() {
		return previousValidation;
	}

	public void setPreviousValidation(boolean previousValidation) {
		this.previousValidation = previousValidation;
	}

	public boolean hasPostValidation() {
		return postValidation;
	}

	public void setPostValidation(boolean postValidation) {
		this.postValidation = postValidation;
	}
	
	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

}
