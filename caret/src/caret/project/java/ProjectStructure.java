package caret.project.java;

import java.util.*;

import caret.data.Resource;

public class ProjectStructure {
    private final Map<String, PackageNode> packages = new HashMap<>();

    public void addResource(Resource resource) {
        String packagePath = extractPackage(resource.getProjectRelativePath());
        String className = resource.getFileName();

        packages.computeIfAbsent(packagePath, PackageNode::new).addClass(className);
    }

    private String extractPackage(String relativePath) {
        // Remove "src/" prefix if present
        if (relativePath.startsWith("src/")) {
            relativePath = relativePath.substring(4);
        }
        
        // Remove filename and convert path to package notation
        int lastSlash = relativePath.lastIndexOf('/');
        if (lastSlash == -1) return ""; // Default package
        return relativePath.substring(0, lastSlash).replace('/', '.');
    }

    public void printStructure() {
        for (PackageNode pkg : packages.values()) {
            System.out.println(pkg);
        }
    }
}

