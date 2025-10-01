package caret.project.java;

import java.util.*;

public class PackageNode {
    private final String packageName;
    private final Set<String> classFiles = new TreeSet<>();

    public PackageNode(String packageName) {
        this.packageName = packageName;
    }

    public void addClass(String classFile) {
        classFiles.add(classFile);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Package: " + packageName + "\n");
        for (String classFile : classFiles) {
            sb.append("  - ").append(classFile).append("\n");
        }
        return sb.toString();
    }
}

