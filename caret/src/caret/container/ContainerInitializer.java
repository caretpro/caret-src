package caret.container;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ContainerInitializer extends ClasspathContainerInitializer {

    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException {

        JavaCore.setClasspathContainer(
                containerPath,
                new IJavaProject[] { project },
                new IClasspathContainer[] { new ClasspathContainer() },
                null);
    }
}