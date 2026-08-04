package caret.container;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class ClasspathContainer implements IClasspathContainer {

    public static final IPath CONTAINER_PATH = new Path("CARET_CONTAINER");
    private final IClasspathEntry[] entries;

    public ClasspathContainer() {
        IClasspathEntry[] tempEntries = new IClasspathEntry[0];

        try {
            Bundle bundle = Platform.getBundle("caret");
            if (bundle == null) {
                throw new IllegalStateException("Bundle caret not found");
            }

            URL url = FileLocator.find(bundle, new Path("lib/caret.annotation.jar"), null);
            if (url == null) {
                throw new IllegalStateException("caret.annotation.jar not found in lib folder");
            }

            URL resolvedUrl = FileLocator.resolve(url);
            File jarFile = new File(resolvedUrl.toURI());

            tempEntries = new IClasspathEntry[] {
                JavaCore.newLibraryEntry(new Path(jarFile.getAbsolutePath()), null, null)
            };
            
            

        } catch (Exception e) {
            e.printStackTrace();
        }

        entries = tempEntries;
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
        return entries;
    }

    @Override
    public String getDescription() {
        return "Caret";
    }

    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return CONTAINER_PATH;
    }
}