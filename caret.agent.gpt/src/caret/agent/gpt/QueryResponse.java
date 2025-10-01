package caret.agent.gpt;

public class QueryResponse {

	private String id;
	private String object;
	private long created;
	private Choices [] choices;
	private String finish_reason;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public Choices [] getChoices() {
		return choices;
	}
	public void setChoices(Choices [] choices) {
		this.choices = choices;
	}
	public String getFinish_reason() {
		return finish_reason;
	}
	public void setFinish_reason(String finish_reason) {
		this.finish_reason = finish_reason;
	}
	
}

