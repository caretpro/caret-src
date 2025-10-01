package caret.preferences;

public class ItemData {
    private String task;
    private String description;

    public ItemData(String task, String description) {
        this.task = task;
        this.description = description;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
