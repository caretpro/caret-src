package caret.data;

import java.util.Date;

import caret.tasks.Parameter;

public class Interaction {

	private long timestamp;
	private String role;
	private String text;
	private String code;
	private String hash;
	private Context context;
	private Result result;
	private String taskCode;
	private String taskName;
	private String targetParameterType;
	private String targetParameterName;
	private String chatMessage;

	public Interaction() {
		this.timestamp = new Date().getTime();
	}

	public Interaction(String role, String text, String code, Context context, String taskCode) {
		this.timestamp = new Date().getTime();
		this.role = role;
		this.text = text;
		this.code = code;
		this.context = context;
		this.taskCode = taskCode;
	}
	public Interaction(String role, String text, String code, Context context, String taskCode, long timestamp) {
		this.timestamp = timestamp;
		this.role = role;
		this.text = text;
		this.code = code;
		this.context = context;
		this.taskCode = taskCode;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	
	public Result getResult() {
		return result;
	}
	
	public void setResult(Result result) {
		this.result = result;
	}
	public String getTaskCode() {
		return taskCode;
	}
	public void setTaskCode(String taskCode) {
		this.taskCode = taskCode;
	}
	
	public String getTargetParameterType() {
		return targetParameterType;
	}

	public void setTargetParameterType(String targetParameterType) {
		this.targetParameterType = targetParameterType;
	}

	public String getTargetParameterName() {
		return targetParameterName;
	}

	public void setTargetParameterName(String targetParameterName) {
		this.targetParameterName = targetParameterName;
	}
	
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
	public String getChatMessage() {
		return chatMessage;
	}

	public void setChatMessage(String chatMessage) {
		this.chatMessage = chatMessage;
	}
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
