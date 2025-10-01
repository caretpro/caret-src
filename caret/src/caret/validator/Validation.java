package caret.validator;

public class Validation {
    private boolean valid;
    private String errorMessage;
    private String info;
    private String errorStackTrace;

	public Validation() {
    	
    }

    public Validation(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String setInfo(String info) {
        return this.info = info;
    }
    
    public String getInfo() {
        return info;
    }
    
    public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public void setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}
}