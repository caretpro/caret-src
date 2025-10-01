package caret.agent.codex;

public class QueryResponse {
    private String id;
    private String object;
    private long created_at;
    private String status;
    private Object error;
    private Object instructions;
    private String model;
    private Output[] output;
    private Usage usage;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }

    public long getCreated_at() { return created_at; }
    public void setCreated_at(long created_at) { this.created_at = created_at; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }

    public Object getInstructions() { return instructions; }
    public void setInstructions(Object instructions) { this.instructions = instructions; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Output[] getOutput() { return output; }
    public void setOutput(Output[] output) { this.output = output; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }
}


	
	
	


