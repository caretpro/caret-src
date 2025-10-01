package caret.tasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parameter {

	private String name;
	private String description;
	private boolean required = false;
	private String value;
	private String noValue;
	private ParameterType parameterType;
	private boolean hasSource = false;
	private String source;

	public Parameter() {
		
	}

	public Parameter(String name, String description, ParameterType parameterType) {
		setName(name);
		this.description = description;
		this.parameterType = parameterType;
	}
	
	public Parameter(String name, String description, ParameterType parameterType, boolean required, boolean hasSource) {
		setName(name);
		this.description = description;
		this.parameterType = parameterType;
		this.required = required;
		this.hasSource = hasSource;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		this.noValue = getNoValue(name);
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public ParameterType getParameterType() {
		return parameterType;
	}

	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}
	
	public String getNoValue() {
		return noValue;
	}

	public void setNoValue(String noValue) {
		this.noValue = noValue;
	}
	
	public boolean hasSource() {
		return hasSource;
	}

	public void setHasSource(boolean hasSource) {
		this.hasSource = hasSource;
	}
	
	private String getNoValue(String name) {
		Pattern stringPattern = Pattern.compile("(\\b[a-z]+)|(^[a-z]+)|([A-Z][a-z]*)");
	    Matcher matcher = stringPattern.matcher(name);
	    String text = "";
	    boolean first = true;
	    while(matcher.find()) {
	    	if(first) {
	    		text += "NO_"+matcher.group().toUpperCase();
	    		first = false;
	    	}else {
	    		text += "_"+matcher.group().toUpperCase();
	    	}
	    }
	    return text;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}
