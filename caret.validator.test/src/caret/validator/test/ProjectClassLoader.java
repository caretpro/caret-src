package caret.validator.test;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;

public class ProjectClassLoader {

    public static Class<?>[] getClassesFromProjectAndPackage(IProject project, String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        if (project.isAccessible()) {
            IResource binFolder = project.findMember("bin");

            if (binFolder != null && binFolder.exists() && binFolder instanceof IContainer) {
                File binDirectory = binFolder.getLocation().toFile();
                URL binUrl = binDirectory.toURI().toURL();
                System.out.println("*urlbin: "+binUrl);
                URLClassLoader classLoader = new URLClassLoader(new URL[] { binUrl });
                findClasses((IContainer) binFolder, "", classLoader, classList, packageName);
            }
        }
        return classList.toArray(new Class<?>[0]);
    }
    
    public static ClassLoader  getClassLoaderfromProjectAndPackage(IProject project, String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        if (project.isAccessible()) {
            IResource binFolder = project.findMember("bin");

            if (binFolder != null && binFolder.exists() && binFolder instanceof IContainer) {
                File binDirectory = binFolder.getLocation().toFile();
                URL binUrl = binDirectory.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[] { binUrl });

                return classLoader;
            }
        }
        return null;
    }

    private static void findClasses(IContainer container, String currentPackage, ClassLoader classLoader, List<Class<?>> classList, String targetPackage) throws CoreException {
        for (IResource resource : container.members()) {
            if (resource instanceof IContainer) {
                String newPackage = currentPackage.isEmpty() ? resource.getName() : currentPackage + "." + resource.getName();
                findClasses((IContainer) resource, newPackage, classLoader, classList, targetPackage);
            } else if (resource.getName().endsWith(".class")) {
                String className = resource.getName().replace(".class", "");
                String qualifiedClassName = currentPackage.isEmpty() ? className : currentPackage + "." + className;
                if (qualifiedClassName.startsWith(targetPackage)) {
                    try {
                        Class<?> clazz = classLoader.loadClass(qualifiedClassName);
                        classList.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace(); 
                    }
                }
            }
        }
    }
}
