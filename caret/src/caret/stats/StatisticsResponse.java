package caret.stats;

public class StatisticsResponse {

    private String classificationCode;
    private String javaClass;

    // Required no-args constructor for Gson
    public StatisticsResponse() {}

    public String getClassificationCode() {
        return classificationCode;
    }

    public void setClassificationCode(String classificationCode) {
        this.classificationCode = classificationCode;
    }

    public String getJavaClass() {
        return javaClass;
    }

    public void setJavaMethod(String javaMethod) {
        this.javaClass = javaMethod;
    }

    @Override
    public String toString() {
        return "StatisticsResponse{" +
                "classificationCode='" + classificationCode + '\'' +
                ", javaClass='" + javaClass + '\'' +
                '}';
    }
}

