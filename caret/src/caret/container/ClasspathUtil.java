package caret.container;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import caret.tool.Log;

public final class ClasspathUtil {

    private ClasspathUtil() {
    }

    public static void addBoosterLibrary(IProject project) throws Exception {
    	Log.d("Adding library to: "+project.getName());
        if (project == null || !project.isAccessible()) {
            return;
        }

        if (!project.hasNature(JavaCore.NATURE_ID)) {
            return;
        }

        IJavaProject javaProject = JavaCore.create(project);
        IPath containerPath = ClasspathContainer.CONTAINER_PATH;
        IClasspathEntry[] entries = javaProject.getRawClasspath();

        for (IClasspathEntry entry : entries) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER
                    && entry.getPath().equals(containerPath)) {
            	Log.d("Caret containter found.");
                return;
            }
        }

        IClasspathEntry boosterEntry = JavaCore.newContainerEntry(containerPath);
        IClasspathEntry[] newEntries = Arrays.copyOf(entries, entries.length + 1);
        newEntries[entries.length] = boosterEntry;

        javaProject.setRawClasspath(newEntries, null);
        Log.d("Caret containter added!.");
    }
}