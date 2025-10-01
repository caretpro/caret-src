package caret.validator.test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import caret.ChatView;
import caret.tool.Log;
import caret.validator.ValidatorInterface;
import caret.validator.test.property.TestPropertyConstants;

public class TestValidator implements ValidatorInterface {
	
	private String id = "Test";
	private String name = "Test Validator";
	private String validationType = "Test";
	private DynamicClassTester tester;
	private String errorMessage = "";
	private int count = 0;
	ChatView chatView = ChatView.getInstance();
	private String info = null;
	private String errorStackTrace = null;
	
	public TestValidator() {
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
		count++;
		IProject project = null;
		System.out.println("---is valid:"+count);
		if(iResource == null) {
	    	project = chatView.getCurrentProject();
	    	System.out.println("#Test Validator applying:"+project.getName());
		}
		boolean valid = false;
        //valid = TestRunner.runTests(testDirectory);
		String testFinderType = null;
		TestRunner testRunner = new TestRunner(project);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<?> future = executor.submit(testRunner);
		try {
		    try {
				future.get(15, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.errorMessage = "InterruptedException. Cancelling TestRunner.";
			    System.out.println(errorMessage);
			    future.cancel(true);
			} catch (ExecutionException e) {
				e.printStackTrace();
				this.errorMessage = "ExecutionException. Cancelling TestRunner.";
			    System.out.println(errorMessage);
			    future.cancel(true);
			}
		} catch (TimeoutException e) {
			this.errorMessage = "TimeoutException. Cancelling TestRunner.";
		    System.out.println(errorMessage);
		    future.cancel(true);  
		}
		System.out.println("Finished ExecutorService TestRunner");
		valid = testRunner.isValid();
		info = testRunner.getTestInfo();
		if(!valid) {
			this.errorMessage = testRunner.getErrorMessage();
		}
		executor.shutdownNow();
		return valid;
	}
	
	public boolean isValid(Class<?> clazz) {
		this.tester = new DynamicClassTester(clazz);
		return false;
	}
	
	
    @TestFactory
    public List<DynamicTest> dynamicTests() {
        return tester.generateTests();
    }

	@Override
	public boolean isPreviousValidation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPostValidation() {
		// TODO Auto-generated method stub
		return true;
	}
	

	@Override
	public boolean isReady() {
		if(chatView.getCurrentProject()!=null) {
			Log.d("Test-> Project: "+chatView.getCurrentProject().getName());
			ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(new ProjectScope(chatView.getCurrentProject()), Activator.PLUGIN_ID);
			String testpath = preferenceStore.getString(TestPropertyConstants.PROPERTY_TEST_FOLDER);
			String regex = preferenceStore.getString(TestPropertyConstants.PROPERTY_TEST_REGEX);
			String testclass = preferenceStore.getString(TestPropertyConstants.PROPERTY_TEST_CLASS);
			boolean enabledTestpath = preferenceStore.getBoolean(TestPropertyConstants.PROPERTY_ENABLE_TEST_FOLDER);
			boolean enabledTestRegex = preferenceStore.getBoolean(TestPropertyConstants.PROPERTY_ENABLE_TEST_REGEX);
			boolean enabledTestClass = preferenceStore.getBoolean(TestPropertyConstants.PROPERTY_ENABLE_TEST_CLASS);
			if((check(testpath) && enabledTestpath)||(check(regex) && enabledTestRegex)||(check(testclass) && enabledTestClass)) {
				return true;
			}else {
				return false;
			}
		}else {
			Log.d("Test-> Project: NULL");
			return false;
		}
		
	}
	
	private boolean check(String value) {
		if(value != null) {
			if(!value.trim().equals("")) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}

	@Override
	public String getError() {
		return this.errorMessage;
	}

	public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(){
		return null;
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