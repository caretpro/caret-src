package caret.vcs;

import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import java.io.File;

public class GitUser {

    private String user;
    private String mail;

    public void loadGitUser() {
        try {
            File gitConfigFile = new File(FS.DETECTED.userHome(), ".gitconfig");
            FileBasedConfig config = new FileBasedConfig(gitConfigFile, FS.DETECTED);
            config.load();
            this.user = config.getString("user", null, "name");
            this.mail = config.getString("user", null, "email");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printGitUser() {
        if (user != null && mail != null) {
        	System.out.println("# Detected .gitconfig");
            System.out.println("# Git user: " + user + " <" + mail + ">");
        } else {
            System.out.println("No Git user configured in ~/.gitconfig");
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
