package caret.tool;

public class MethodPosition {

    private String methodName;
    private int lineInitial;
    private int lineEnd;

    public MethodPosition(String methodName, int lineInitial, int lineEnd) {
        this.methodName = methodName;
        this.lineInitial = lineInitial;
        this.lineEnd = lineEnd;
    }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public int getLineInitial() { return lineInitial+1; }
    public void setLineInitial(int lineInitial) { this.lineInitial = lineInitial; }
    public int getLineEnd() { return lineEnd+1; }
    public void setLineEnd(int lineEnd) { this.lineEnd = lineEnd; }
}
