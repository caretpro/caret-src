package caret.data;

public class Agent {

	String name;
	String technology;
	boolean isLLM;
	
	public Agent(String name, String technology, boolean isLLM) {
		this.name = name;
		this.technology = technology;
		this.isLLM = isLLM;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTechnology() {
		return technology;
	}
	public void setTechnology(String technology) {
		this.technology = technology;
	}
	public boolean isLLM() {
		return isLLM;
	}
	public void setLLM(boolean isLLM) {
		this.isLLM = isLLM;
	}
	
}
