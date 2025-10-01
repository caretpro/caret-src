package caret.tool;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import caret.Activator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClasspathLoader {

    public static List<File> loadClasspathFromProject(IProject project) {
        List<File> classpathFiles = new ArrayList<>();
        try {
            IFile classpathFile = project.getFile(new Path(".classpath"));
            
            if (!classpathFile.exists()) {
                System.out.println("The file .classpath doesn't exist in the project.");
                return classpathFiles;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(classpathFile.getContents());
            
            NodeList entries = document.getElementsByTagName("classpathentry");
            for (int i = 0; i < entries.getLength(); i++) {
                String kind = entries.item(i).getAttributes().getNamedItem("kind").getNodeValue();
                String path = entries.item(i).getAttributes().getNamedItem("path").getNodeValue();
                
                if (kind.equals("lib") || kind.equals("src")) {
                    File entryFile;
                    if (path.startsWith(File.separator)) {
                    	entryFile = new File(path);
                    } else {
                    	entryFile = new File(project.getLocation().toFile(), path);
                    }
                    if (entryFile.exists()) {
                        classpathFiles.add(entryFile);
                        System.out.println("the file exists in the classpath: " + path);
                    } else {
                        System.out.println("\"The file was not found in the classpath: " + path);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error to process .classpath: " + e.getMessage());
        }

        return classpathFiles;
    }
    
    public static void addRelativeJarToClasspath(IProject project, String relativePath){
        try {
			if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
			    IJavaProject javaProject = JavaCore.create(project);
			    IClasspathEntry[] existingEntries = javaProject.getRawClasspath();
			    
			    // Resolve the absolute path using FileLocator
			    URL fileURL = FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry(relativePath));
			    File jarFile = new File(fileURL.getPath());
			    String jarAbsolutePath = jarFile.getAbsolutePath();

			    IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(jarAbsolutePath), null, null);

			    // Check if the entry already exists in the classpath
			    for (IClasspathEntry entry : existingEntries) {
			        if (entry.getPath().equals(new Path(jarAbsolutePath))) {
			            System.out.println("JAR is already in the classpath.");
			            return;
			        }
			    }

			    // Add the new entry to the classpath
			    IClasspathEntry[] newEntries = new IClasspathEntry[existingEntries.length + 1];
			    System.arraycopy(existingEntries, 0, newEntries, 0, existingEntries.length);
			    newEntries[existingEntries.length] = jarEntry;

			    javaProject.setRawClasspath(newEntries, null);
			    System.out.println("JAR added to classpath.");
			} else {
			    System.out.println("Project is not a Java project or is not open.");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
