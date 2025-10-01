package caret.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.DiagnosticCollector;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler.CompilationTask;

import org.eclipse.core.resources.IProject;

import caret.ChatView;


public class JavaSourceCompiler {
	
	String errorMessage = "";
	
	List<Diagnostic<? extends JavaFileObject>> diagnostics = null;

	public JavaSourceCompiler() {
		
	}

	public boolean compile(String name, String source) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new JavaFileString(name, source);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        //CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        IProject project = ChatView.getInstance().getCurrentProject();
        List<File> classpathFiles = ClasspathLoader.loadClasspathFromProject(project);
        List<String> options = new ArrayList<>();
        if (classpathFiles != null && !classpathFiles.isEmpty()) {
            StringBuilder classpath = new StringBuilder();
            for (File path : classpathFiles) {
                classpath.append(path.getPath()).append(File.pathSeparator);
            }
            options.addAll(Arrays.asList("-classpath", classpath.toString()));
        }
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();
        this.diagnostics = diagnostics.getDiagnostics();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        	/*errorMessage = String.format("Error in line %d in %s%n",
                    diagnostic.getLineNumber(),
                    diagnostic.getSource().toUri())
                    + diagnostic.getMessage(null);*/
        	JavaFileObject tempSource = diagnostic.getSource();
    	    String sourceInfo = (tempSource != null) ? tempSource.toUri().toString() : "<no source>";
    	    errorMessage = String.format("Error in line %d in %s%n",
    	            diagnostic.getLineNumber(),
    	            sourceInfo)
    	            + diagnostic.getMessage(null);
    	    System.out.println(errorMessage);
        }
        return success;
    }
	
    public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(){
		return this.diagnostics;
	}
}

