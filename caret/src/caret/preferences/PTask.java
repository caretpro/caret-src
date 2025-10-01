package caret.preferences;
import java.util.HashMap;
import java.util.List;

public class PTask {
    private boolean enabled;
    private String taskName;
    private HashMap<String, Boolean> validators;
    private HashMap<String, Boolean> context;

	// Constructor
    public PTask(boolean enabled, String taskName, HashMap<String, Boolean> validators, HashMap<String, Boolean> context) {
        this.enabled = enabled;
        this.taskName = taskName;
        this.validators = validators;
        this.context = context;
    }

    // Getters and setters (optional)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public HashMap<String, Boolean> getValidators() {
        return validators;
    }

    public void setValidators(HashMap<String, Boolean> validators) {
        this.validators = validators;
    }
    
    public HashMap<String, Boolean> getContext() {
		return context;
	}

	public void setContext(HashMap<String, Boolean> context) {
		this.context = context;
	}
}
