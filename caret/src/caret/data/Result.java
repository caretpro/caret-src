package caret.data;

public class Result {
	
	boolean used;
	boolean createdResource;
	Agent agent;
	
	public Result(Agent agent, boolean used, boolean createdResource){
		this.agent = agent;
		this.used = used;
		this.createdResource = createdResource;
	}
	
	public boolean isUsed() {
		return used;
	}
	
	public void setUsed(boolean used) {
		this.used = used;
	}
	
	public boolean isCreatedResource() {
		return createdResource;
	}
	
	public void setCreatedResource(boolean createdResource) {
		this.createdResource = createdResource;
	}
	
	public Agent getAgent() {
		return agent;
	}
	
	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	
}
