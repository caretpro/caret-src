package caret.agent.codex;
import com.google.gson.annotations.SerializedName;

public class Usage {

    @SerializedName("input_tokens")
    private int inputTokens;

    @SerializedName("output_tokens")
    private int outputTokens;

    @SerializedName("total_tokens")
    private int totalTokens;

    public int getInputTokens() {
        return inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public int getTotalTokens() {
        return totalTokens;
    }
}