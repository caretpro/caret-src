package caret.service;

public class ResponseModification {
    private String methodName;
    private boolean requiredModification;
    private String modifiedMethod;
    private String explanation;

    public ResponseModification(String methodName, boolean requiredModification, String modifiedMethod, String explanation) {
        this.methodName = methodName;
        this.requiredModification = requiredModification;
        this.modifiedMethod = modifiedMethod;
        this.explanation = explanation;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isRequiredModification() {
        return requiredModification;
    }

    public void setRequiredModification(boolean requiredModification) {
        this.requiredModification = requiredModification;
    }

    public String getModifiedMethod() {
        return modifiedMethod;
    }

    public void setModifiedMethod(String modifiedMethod) {
        this.modifiedMethod = modifiedMethod;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

}