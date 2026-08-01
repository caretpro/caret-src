package game;

public class User {
    private String name;
    private String level;

    public User(String name, String level) {
	    this.name = name;
	    this.level = level;
	}

    public String getName() { return name; }
    public String getLevel() { return level; }
}
