package caret.agent;

public interface AgentInterface {
	
	public String getId();
	
	public String getName();
	
	public String getTechnology();
	
	public boolean hasIntent();

	public void setKey(String key);
	
	public String getKey();
	
	public Response processMessage(String message, float temperature);
	
	public boolean isLLM();
	
}

