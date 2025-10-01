package caret.validator.compilation;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IResource;

import caret.tool.JavaSourceCompiler;
import caret.tool.Log;
import caret.tool.Parser;
import caret.tool.Util;
import caret.validator.ValidatorInterface;

public class CompilationValidator implements ValidatorInterface {
	
	private String id = "Compilation";
	private String name = "Compilation Validator";
	private String validationType = "Compilation";
	private String errorMessage = "";
	List<Diagnostic<? extends JavaFileObject>> diagnostics;
	private String info = null;
	private String errorStackTrace = null;
	
	public CompilationValidator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValidationType() {
		return validationType;
	}

	@Override
	public boolean isValid(IResource iResource, String modifiedCode) {
		if(iResource != null) {
			Log.d("isValid->iResource: "+iResource.getName());
		}
		boolean valid = false;
        try {
    		String name = Parser.getClassName(modifiedCode, Parser.TYPE_CONTENT);
    		if(name != null) {
    			JavaSourceCompiler javaSourceCompiler= new JavaSourceCompiler();
                valid = javaSourceCompiler.compile(name, modifiedCode);
                diagnostics = javaSourceCompiler.getDiagnostics();
                if(!valid) {
                	errorMessage = javaSourceCompiler.getErrorMessage();
                }
    		}else {
    			valid = false;
    			errorMessage = "The source code is not a valid complete Java class, and the class name could not be detected. Java class compilation cannot be validated";
    		}
            return valid;
	        
		} catch (Exception e) {
			e.printStackTrace();
			errorStackTrace = Util.getStackTraceAsString(e);
		}
        return valid;
	}

	@Override
	public boolean isPreviousValidation() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isPostValidation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getError() {
		// TODO Auto-generated method stub
		return this.errorMessage;
	}
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(){
		return this.diagnostics;
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public String getErrorStackTrace() {
		return errorStackTrace;
	}

}
