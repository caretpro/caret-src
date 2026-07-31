package caret.agent;

import com.google.gson.annotations.SerializedName;

public class Usage {

    @SerializedName("completion_tokens")
    private int completionTokens;

    @SerializedName("prompt_tokens")
    private int promptTokens;

    @SerializedName("total_tokens")
    private int totalTokens;

    public Usage() {
    }

    public Usage(int completionTokens, int promptTokens, int totalTokens) {
        this.completionTokens = completionTokens;
        this.promptTokens = promptTokens;
        this.totalTokens = totalTokens;
    }

    public int getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    public int getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    @Override
    public String toString() {
        return "Usage{" +
                "completionTokens=" + completionTokens +
                ", promptTokens=" + promptTokens +
                ", totalTokens=" + totalTokens +
                '}';
    }
}