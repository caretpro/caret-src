package caret.tool;
import javax.tools.*;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class InMemoryJavaCompiler {

    // -------------------------------------------------------
    // 1. Java source stored in memory
    // -------------------------------------------------------
    static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;

        JavaSourceFromString(String className, String code) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    // -------------------------------------------------------
    // 2. Compiled .class stored in memory
    // -------------------------------------------------------
    static class MemoryClassFile extends SimpleJavaFileObject {
        private final ByteArrayOutputStream byteCode = new ByteArrayOutputStream();

        MemoryClassFile(String className, Kind kind) {
            super(URI.create("mem:///" + className.replace('.', '/') + kind.extension),
                    kind);
        }

        @Override
        public OutputStream openOutputStream() {
            return byteCode;
        }

        byte[] getBytes() {
            return byteCode.toByteArray();
        }
    }

    // -------------------------------------------------------
    // 3. ClassLoader for loading compiled in-memory classes
    // -------------------------------------------------------
    static class MemoryClassLoader extends ClassLoader {
        private final Map<String, MemoryClassFile> compiledClasses = new HashMap<>();
        private final ClassLoader pluginClassLoader;

        public MemoryClassLoader(ClassLoader pluginClassLoader) {
            super(pluginClassLoader); // delega al plugin
            this.pluginClassLoader = pluginClassLoader;
        }

        void addClass(String className, MemoryClassFile classFile) {
            compiledClasses.put(className, classFile);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            MemoryClassFile classFile = compiledClasses.get(name);
            if (classFile != null) {
                byte[] bytes = classFile.getBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            // delega al plugin classloader
            return pluginClassLoader.loadClass(name);
        }
    }


    // -------------------------------------------------------
    // 4. Main method: compile + load + execute
    // -------------------------------------------------------
    public static Object compileAndRun(String className,
                                       String sourceCode,
                                       String methodName, String param) throws Exception {

        // Get system Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No Java compiler found. Make sure you are running a JDK, not a JRE.");
        }

        JavaFileObject sourceFile = new JavaSourceFromString(className, sourceCode);

        List<JavaFileObject> compilationUnits = Collections.singletonList(sourceFile);

        MemoryClassLoader memoryClassLoader = new MemoryClassLoader(
                InMemoryJavaCompiler.class.getClassLoader()
        );

        
        Map<String, MemoryClassFile> outputClassFiles = new HashMap<>();

        // File manager to capture compiled .class bytes into memory
        JavaFileManager fileManager = new ForwardingJavaFileManager<>(
                compiler.getStandardFileManager(null, null, null)) {

            @Override
            public JavaFileObject getJavaFileForOutput(Location location,
                                                       String outClassName,
                                                       JavaFileObject.Kind kind,
                                                       FileObject sibling) {
                MemoryClassFile classFile = new MemoryClassFile(outClassName, kind);
                outputClassFiles.put(outClassName, classFile);
                return classFile;
            }
        };


		String classpath = buildSafeClassPath();
		
		List<String> options = Arrays.asList(
		    "-classpath", classpath
		);
		
		boolean success = compiler.getTask(
		    null, 
		    fileManager, 
		    null, 
		    options, 
		    null, 
		    compilationUnits
		).call();

        // Execute compilation
        //boolean success = compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();

        if (!success) {
            throw new RuntimeException("Compilation failed.");
        }

        // Register compiled classes in the class loader
        for (Map.Entry<String, MemoryClassFile> entry : outputClassFiles.entrySet()) {
            memoryClassLoader.addClass(entry.getKey(), entry.getValue());
        }

        // Load and execute the method
        Class<?> clazz = memoryClassLoader.loadClass(className);
        Method method = clazz.getMethod(methodName, String.class);

        Object instance = clazz.getDeclaredConstructor().newInstance();
        return method.invoke(instance, param);
    }
    
	
	private static Bundle getThisBundle() {
	    return FrameworkUtil.getBundle(InMemoryJavaCompiler.class);
	}
	
	private static List<Bundle> getRequiredBundles(Bundle bundle) {
	    List<Bundle> list = new ArrayList<>();
	
	    String reqBundles = bundle.getHeaders().get("Require-Bundle");
	
	    if (reqBundles == null) return list;
	
	    String[] names = reqBundles.split(",");
	
	    BundleContext ctx = bundle.getBundleContext();
	
	    for (String name : names) {
	        String bname = name.split(";")[0].trim();
	        for (Bundle b : ctx.getBundles()) {
	            if (b.getSymbolicName().equals(bname)) {
	                list.add(b);
	                break;
	            }
	        }
	    }
	
	    return list;
	}
	
	private static List<String> getBundlePhysicalClassPath(Bundle bundle) throws Exception {
	
	    List<String> list = new ArrayList<>();
	
	    // 1. Si el bundle es un JAR
	    File bundleFile = FileLocator.getBundleFile(bundle);
	    if (bundleFile.isFile() && bundleFile.getName().endsWith(".jar")) {
	        list.add(bundleFile.getAbsolutePath());
	        return list;
	    }
	
	    // 2. Caso PDE: carpeta raíz del proyecto plugin
	    URL rootEntry = bundle.getEntry("/");
	    if (rootEntry != null) {
	        URL resolved = FileLocator.toFileURL(rootEntry);
	        File root = new File(resolved.getPath());
	
	        // /bin
	        File bin = new File(root, "bin");
	        if (bin.exists()) list.add(bin.getAbsolutePath());
	
	        // /target/classes
	        File classes = new File(root, "target/classes");
	        if (classes.exists()) list.add(classes.getAbsolutePath());
	
	        // libs internas del plugin (Bundle-ClassPath)
	        String bcp = bundle.getHeaders().get("Bundle-ClassPath");
	        if (bcp != null) {
	            for (String cpEntry : bcp.split(",")) {
	                cpEntry = cpEntry.trim();
	                URL url = bundle.getEntry(cpEntry);
	                if (url != null) {
	                    URL r = FileLocator.toFileURL(url);
	                    File f = new File(r.getPath());
	                    if (f.exists()) list.add(f.getAbsolutePath());
	                }
	            }
	        }
	    }
	
	    return list;
	}
	
	private static String buildSafeClassPath() throws Exception {
	
	    Bundle thisBundle = getThisBundle();
	    List<Bundle> bundles = new ArrayList<>();
	
	    bundles.add(thisBundle);
	    bundles.addAll(getRequiredBundles(thisBundle));
	
	    StringBuilder cp = new StringBuilder();
	
	    for (Bundle b : bundles) {
	        for (String p : getBundlePhysicalClassPath(b)) {
	            cp.append(p).append(File.pathSeparator);
	        }
	    }
	
	    return cp.toString();
	}
	

}
