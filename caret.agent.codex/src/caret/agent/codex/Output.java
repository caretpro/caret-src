package caret.agent.codex;

public class Output {
    private String type;
    private String id;
    private String status;
    private String role;
    private Content[] content;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Content[] getContent() { return content; }
    public void setContent(Content[] content) { this.content = content; }
}
