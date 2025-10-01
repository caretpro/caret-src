package caret.agent;

import java.util.ArrayList;
import java.util.HashMap;

public class Response {
	
	private String sessionId;
	private String text;
	private String intent;
	private String context;
	private String code;
	private Boolean allRequiredParams = false;
	private Boolean fallbackIntent = false;
	private HashMap<String, String> parameters;
	private boolean error = false;
	private String agentId;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	private String errorMessage = null;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public Boolean getAllRequiredParams() {
		return allRequiredParams;
	}

	public void setAllRequiredParams(Boolean allRequiredParams) {
		this.allRequiredParams = allRequiredParams;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public Boolean getFallbackIntent() {
		return fallbackIntent;
	}

	public void setFallbackIntent(Boolean fallbackIntent) {
		this.fallbackIntent = fallbackIntent;
	}
	
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
}
