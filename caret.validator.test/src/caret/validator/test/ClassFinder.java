package caret.validator.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import caret.tool.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ClassFinder {

    public static Class<?>[] getClassByName(IProject project, String className, ClassLoader classLoader) throws ClassNotFoundException, IOException, CoreException {
        if (project == null || !project.isOpen()) {
            throw new IllegalArgumentException("The project is not valid or is not open.");
        }

        File binDirectory = getBinDirectory(project);
        if (binDirectory == null) {
            throw new IOException("Could not find the 'bin' directory for the project: " + project.getName());
        }

        return new Class<?>[]{classLoader.loadClass(className)};
        
    }

    public static Class<?>[] getClassesByRegex(IProject project, String packageName, String regex, ClassLoader classLoader) throws IOException, ClassNotFoundException, CoreException {
        if (project == null || !project.isOpen()) {
            throw new IllegalArgumentException("The project is not valid or is not open.");
        }

        File binDirectory = getBinDirectory(project);
        if (binDirectory == null) {
            throw new IOException("Could not find the 'bin' directory for the project: " + project.getName());
        }

        File packageDirectory = new File(binDirectory, packageName.replace('.', '/'));
        if (!packageDirectory.exists()) {
            throw new IOException("Package directory does not exist: " + packageDirectory.getAbsolutePath());
        }

        Pattern pattern = Pattern.compile(regex);
        List<Class<?>> matchedClasses = new ArrayList<>();
        File[] files = packageDirectory.listFiles((dir, name) -> name.endsWith(".class"));
        if (files != null) {
                for (File file : files) {
                    String className = file.getName().substring(0, file.getName().length() - 6); // Remove ".class"
                    Matcher matcher = pattern.matcher(className);
                    if (matcher.matches()) {
                        matchedClasses.add(classLoader.loadClass(packageName + '.' + className));
                    }
                }
            
        }

        return matchedClasses.toArray(new Class<?>[0]);
    }

    public static Class<?>[] getClassesFromDirectory(IProject project, String packageName, ClassLoader classLoader) throws ClassNotFoundException, IOException, CoreException {
        if (project == null || !project.isOpen()) {
            throw new IllegalArgumentException("The project is not valid or is not open.");
        }

        File binDirectory = getBinDirectory(project);
        if (binDirectory == null) {
            throw new IOException("Could not find the 'bin' directory for the project: " + project.getName());
        }

        File packageDirectory = new File(binDirectory, packageName.replace('.', '/'));
        if (!packageDirectory.exists()) {
            throw new IOException("Package directory does not exist: " + packageDirectory.getAbsolutePath());
        }

        List<Class<?>> classes = new ArrayList<>();
        
        File[] files = packageDirectory.listFiles((dir, name) -> {
            if (!name.endsWith(".class")) return false;

            String className = name.substring(0, name.length() - 6); // remove .class
            return className.matches(".*Test(s)?") || className.matches("Test.*");
        });
        
        if (files != null) {
                for (File file : files) {
                    String className = file.getName().substring(0, file.getName().length() - 6); // Remove ".class"
                    if(packageName.equals("")) {
                    	classes.add(classLoader.loadClass(className));
                    }else {
                    	classes.add(classLoader.loadClass(packageName + '.' + className));
                    }
                }
        }

        return classes.toArray(new Class<?>[0]);
    }

    private static File getBinDirectory(IProject project) throws CoreException {
        IFolder binFolder = project.getFolder("bin");
        if (binFolder.exists()) {
            return new File(binFolder.getLocation().toOSString());
        }
        return null;
    }
    
    public static Class<?>[] getClassesFromDirectoryRecursive(IProject project, String packageName, ClassLoader classLoader) throws ClassNotFoundException, IOException, CoreException {
        if (project == null || !project.isOpen()) {
            throw new IllegalArgumentException("The project is not valid or is not open.");
        }

        File binDirectory = getBinDirectory(project);
        if (binDirectory == null) {
            throw new IOException("Could not find the 'bin' directory for the project: " + project.getName());
        }

        File packageDirectory = new File(binDirectory, packageName.replace('.', '/'));
        if (!packageDirectory.exists()) {
            throw new IOException("Package directory does not exist: " + packageDirectory.getAbsolutePath());
        }

        List<Class<?>> classes = new ArrayList<>();

        loadClassesRecursively(packageDirectory, packageName, classes, classLoader);

        return classes.toArray(new Class<?>[0]);
    }

    private static void loadClassesRecursively(File dir, String packageName, List<Class<?>> classes, ClassLoader classLoader) throws ClassNotFoundException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
            	String subPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                loadClassesRecursively(file, subPackage, classes, classLoader);
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().length() - 6); // Remove ".class"
                if (className.matches(".*Test(s)?") || className.matches("Test.*")) {
                	Log.d("#"+packageName + '.' + className);
                	String fqcn = packageName.isEmpty() ? className : packageName + "." + className;
                    classes.add(classLoader.loadClass(fqcn));
                }
            }
        }
    }


}
