package caret.validator;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IResource;

public interface ValidatorInterface {

	public String getId();
	
	public String getName();
	
	public String getValidationType();
	
	public boolean isValid(IResource iResource, String modifiedCode);
	
	public boolean isPreviousValidation();
	
	public boolean isPostValidation();
	
	public boolean isReady();
	
	public String getError();
	
	public String getInfo();
	
	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics();
	
	public String getErrorStackTrace();
	
}
