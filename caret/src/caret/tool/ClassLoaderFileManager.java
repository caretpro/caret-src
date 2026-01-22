package caret.tool;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ClassLoaderFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final ClassLoader classLoader;

    public ClassLoaderFileManager(JavaFileManager fileManager, ClassLoader classLoader) {
        super(fileManager);
        this.classLoader = classLoader;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location,
                                             String className,
                                             JavaFileObject.Kind kind)
            throws IOException {
        // Try default resolution first
        JavaFileObject fileObject = super.getJavaFileForInput(location, className, kind);
        if (fileObject != null) {
            return fileObject;
        }

        // Try to load class bytes from plugin classloader
        String resource = className.replace('.', '/') + kind.extension;
        InputStream stream = classLoader.getResourceAsStream(resource);

        if (stream == null) {
            return null;
        }

        return new SimpleJavaFileObject(URI.create("pluginclass:///" + resource), kind) {
            @Override
            public InputStream openInputStream() {
                return stream;
            }
        };
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return classLoader;
    }
}
