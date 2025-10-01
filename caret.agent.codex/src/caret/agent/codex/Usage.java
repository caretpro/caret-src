package caret.agent.codex;

public class Usage {
    private int input_tokens;
    private InputTokensDetails input_tokens_details;
    private int output_tokens;
    private OutputTokensDetails output_tokens_details;
    private int total_tokens;

    public int getInput_tokens() { return input_tokens; }
    public void setInput_tokens(int input_tokens) { this.input_tokens = input_tokens; }

    public InputTokensDetails getInput_tokens_details() { return input_tokens_details; }
    public void setInput_tokens_details(InputTokensDetails input_tokens_details) {
        this.input_tokens_details = input_tokens_details;
    }

    public int getOutput_tokens() { return output_tokens; }
    public void setOutput_tokens(int output_tokens) { this.output_tokens = output_tokens; }

    public OutputTokensDetails getOutput_tokens_details() { return output_tokens_details; }
    public void setOutput_tokens_details(OutputTokensDetails output_tokens_details) {
        this.output_tokens_details = output_tokens_details;
    }

    public int getTotal_tokens() { return total_tokens; }
    public void setTotal_tokens(int total_tokens) { this.total_tokens = total_tokens; }
}
