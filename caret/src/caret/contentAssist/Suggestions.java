package caret.contentAssist;

import java.util.HashMap;

import org.eclipse.swt.widgets.Button;

public class Suggestions {
	private static Suggestions suggestions;
	HashMap<String, Suggestion> AgentSuggestions = new HashMap<String, Suggestion>();
	
	private Suggestions() {
		this.suggestions = this;
	}
	
	public static Suggestions getInstance() {
		if (suggestions == null) {
			suggestions = new Suggestions();
		}
    	return suggestions;
    }
	
	public void put(String idAgent, String proposal, String hash, int offset) {
		Suggestion suggestion = new Suggestion(proposal, hash, offset);
		AgentSuggestions.put(idAgent, suggestion);
	}
	
	public String getProposal(String idAgent, String hash, int offset) {
		if(AgentSuggestions.get(idAgent)!=null) {
			return AgentSuggestions.get(idAgent).getProposal(hash, offset);
		}else {
			return null;
		}
	}
}
