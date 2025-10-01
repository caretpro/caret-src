package caret.data;

import java.util.ArrayList;

public class ChatData {
	
	private String sessionId;
	private ArrayList <Interaction> interactions = new ArrayList<Interaction>();
	
	public ChatData() {
		
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	public ArrayList<Interaction> getInteractions() {
		return interactions;
	}
	
	public void addInteraction(Interaction interaction) {
		interactions.add(interaction);
	}
	
	public Interaction getInteraction(int index) {
		return interactions.get(index);
	}
}
