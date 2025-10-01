package caret.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Button;

public class ContextConversation {

    private String sessionId;
    private List<String> messages;
    private List<HashMap<String, String >> listMessages;
    private String project;
    private String packageName;
    private String className;
    private String methodName;

	public ContextConversation() {
        this.messages = new ArrayList<>();
        this.listMessages = new ArrayList<>();
    }

    public ContextConversation(String sessionId, String project, String packageName, String className) {
        this.sessionId = sessionId;
        this.project = project;
        this.packageName = packageName;
        this.className = className;
        this.messages = new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
    
    public void addMessage(String message) {
        this.messages.add(message);
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

}
