package caret.validator.test;

import caret.ChatView;
import caret.project.java.JavaProject;
import caret.tool.Log;
import caret.validator.test.property.TestPropertyConstants;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.nio.file.Paths;

public class TestRunner implements Runnable {
	private int count = 0;
	static private URLClassLoader classLoader;
	private IProject project;
	boolean valid = false;
	TestExecutionSummary executionSummary = null;
	String errorMessage = null;
	String testInfo ="";
	
	public String getTestInfo() {
		return testInfo;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public TestExecutionSummary getExecutionSummary() {
		return executionSummary;
	}

	public boolean isValid() {
		return valid;
	}

	public TestRunner(IProject project) {
		classLoader = getClassLoader(project);
		this.project = project;
	}
	
	@Override
	public void run() {
		try {
			valid = runTests();
        } catch (Exception e) {
            System.out.println("TestRunner was interrupted.");
        }
		
	}
	
    public boolean runTests() {
		boolean valid = false;
    	//String rootPathTest = JavaProject.getPathSource(project);
    	
		Class<?>[] clazzes = null;
		ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(new ProjectScope(project), Activator.PLUGIN_ID);
		String testpath = preferenceStore.getString(TestPropertyConstants.PROPERTY_TEST_FOLDER);
		
    	List <String> srcPaths = JavaProject.getSourcePaths(project);
    	System.out.println("---current project:"+project.getName());
    	String rootPathTest =findRootPathInLargePath(srcPaths, testpath);
    	System.out.println("---current project src:"+rootPathTest);
    	
		String regex = preferenceStore.getString(TestPropertyConstants.PROPERTY_TEST_REGEX);
		String testclass = preferenceStore.getString(TestPropertyConstants.PROPERTY_TEST_CLASS);
		boolean enabledTestpath = preferenceStore.getBoolean(TestPropertyConstants.PROPERTY_ENABLE_TEST_FOLDER);
		boolean enabledTestRegex = preferenceStore.getBoolean(TestPropertyConstants.PROPERTY_ENABLE_TEST_REGEX);
		boolean enabledTestClass = preferenceStore.getBoolean(TestPropertyConstants.PROPERTY_ENABLE_TEST_CLASS);

    	try {
    		if(enabledTestpath) {
	    		System.out.println("---TEST PACKAGE---");
	    		String testpackage = extractPackageName(rootPathTest, testpath);
	    		System.out.println("---testpath:"+testpackage);
	    		//clazzes = ClassFinder.getClassesFromDirectory(project, testpackage, classLoader);
	    		clazzes = ClassFinder.getClassesFromDirectoryRecursive(project, testpackage, classLoader);
	    		valid = runJUnit5(clazzes, classLoader);
    		}
    		if(enabledTestRegex) {
    			System.out.println("---TEST REGEX---");
    			String testpackage = extractPackageName(rootPathTest, testpath);
    			System.out.println("---testpath:"+testpackage);
    			System.out.println("---testregex:"+regex);
        		clazzes = ClassFinder.getClassesByRegex(project, testpackage, regex, classLoader);
        		valid = runJUnit5(clazzes, classLoader);
    		}
    		if(enabledTestClass) {
    			System.out.println("---TEST CLASS---");
    			String testpackageclass = extractClassNameFromPath(rootPathTest, testclass);
    			System.out.println("---testclass:"+testpackageclass);
        		clazzes = ClassFinder.getClassByName(project, testpackageclass, classLoader);
        		valid = runJUnit5(clazzes, classLoader);
    		}
    	} catch (Exception e) {
			e.printStackTrace();
		}
        return valid;
    }
    
    public boolean runJUnit(Class<?>[] clazzes, URLClassLoader classLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
    	boolean fail = false;
    	for (Class<?> clazz : clazzes) {
    		System.out.println("#run test:"+clazz.getName()+"-------");
	    	Class<?> junitCoreClass = classLoader.loadClass("org.junit.runner.JUnitCore");
	    	Object junitCore = junitCoreClass.getDeclaredConstructor().newInstance();
	    	Method runClassesMethod = junitCoreClass.getMethod("runClasses", Class[].class);
	    	Class<?> testClazz = classLoader.loadClass(clazz.getName());
	    	Class<?>[] testClasses = { testClazz };
	    	Object result = runClassesMethod.invoke(junitCore, (Object) testClasses);
	    	Method getFailuresMethod = result.getClass().getMethod("getFailures");
	    	List<?> failures = (List<?>) getFailuresMethod.invoke(result);
	    	for (Object failure : failures) {
	    	    System.out.println("- Failure: "+failure);
	    	    fail = true;
	    	}
	    	if(!fail) {
	    		System.out.println("Test: 0 failures");
	    	}
    	}
    	return !fail;
    }

    public boolean runJUnit5(Class<?>[] clazzes, ClassLoader classLoader) throws Exception {

        Class<?> engineClass = classLoader.loadClass("org.junit.jupiter.engine.JupiterTestEngine");
        JupiterTestEngine engine = (JupiterTestEngine) engineClass.getDeclaredConstructor().newInstance();

        LauncherConfig config = LauncherConfig.builder()
            .enableTestEngineAutoRegistration(false)
            .addTestEngines(engine)
            .build();

        Launcher launcher = LauncherFactory.create(config);

        List<DiscoverySelector> selectors = Arrays.stream(clazzes)
                .map(DiscoverySelectors::selectClass)
                .collect(Collectors.toList());

        LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();
	     requestBuilder.selectors(selectors);
	     LauncherDiscoveryRequest request = requestBuilder.build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        System.out.println("Classes for testing:");
        for (Class<?> c : clazzes) {
            System.out.println(" - " + c.getName() + " -> loaded: " + c.getClassLoader());
        }
        System.out.println("JupiterTestEngine loaded by: " + JupiterTestEngine.class.getClassLoader());
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        
        launcher.execute(request);
 
        System.setOut(originalOut);
        System.setErr(originalErr);
        this.executionSummary = listener.getSummary();

        this.testInfo += "- Total Tests: " + listener.getSummary().getTestsFoundCount();
        this.testInfo += "\n- Tests OK: " + listener.getSummary().getTestsSucceededCount();
        this.testInfo += "\n- Tests Failures: "+listener.getSummary().getFailures().size();
        
        String errorMsg = "";
        TestPlan testPlan = launcher.discover(request);
        List<TestExecutionSummary.Failure> failures = listener.getSummary().getFailures();
        if (failures.size()>0) {
            for (TestExecutionSummary.Failure failure : failures) {
            	TestIdentifier id = failure.getTestIdentifier();
                errorMsg += "\n- Failure in: " + id.getDisplayName();
                Optional<String> parentId = id.getParentId();
                if (parentId.isPresent()) {
                	TestIdentifier parent = testPlan.getTestIdentifier(parentId.get());
                    if (parent != null) {
                        errorMsg += "\nParentDisplayName: " + parent.getDisplayName();
                        Optional<TestSource> sourceOpt = parent.getSource();
                        if (sourceOpt.isPresent()) {
                            TestSource src = sourceOpt.get();
                            if (src instanceof ClassSource) {
                                ClassSource cs = (ClassSource) src;
                                errorMsg += "\nClass: " + cs.getClassName();
                            }
                        }
                    }
                }
                Throwable exception = failure.getException();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                exception.printStackTrace(ps);
                String stackTrace = baos.toString();
                errorMsg += "\n"+stackTrace;
                this.errorMessage = errorMsg;
                
            }
            return false;
        } else {
            return true;
        }
    }

    
    public boolean runJUnit5Reflect(Class<?>[] clazzes, URLClassLoader classLoader) throws Exception {
        boolean fail = false;

        System.out.println("== Init JUnit 5 ==");

        Class<?> launcherFactoryClass = classLoader.loadClass("org.junit.platform.launcher.core.LauncherFactory");
        Method createLauncherMethod = launcherFactoryClass.getMethod("create");
        Object launcher = createLauncherMethod.invoke(null); 

        Class<?> launcherDiscoveryRequestBuilderClass = classLoader.loadClass("org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder");
        Method requestBuilderMethod = launcherDiscoveryRequestBuilderClass.getMethod("request");
        Object requestBuilder = requestBuilderMethod.invoke(null);

        Class<?> discoverySelectorsClass = classLoader.loadClass("org.junit.platform.engine.discovery.DiscoverySelectors");
        Method selectClassMethod = discoverySelectorsClass.getMethod("selectClass", String.class);
        Method buildersAddSelector = launcherDiscoveryRequestBuilderClass.getMethod("selectors", List.class);
        Method buildMethod = launcherDiscoveryRequestBuilderClass.getMethod("build");

        List<Object> selectors = new ArrayList<>();
        for (Class<?> clazz : clazzes) {
            Object selector = selectClassMethod.invoke(null, clazz.getName());
            selectors.add(selector);
        }

        buildersAddSelector.invoke(requestBuilder, selectors);
        Object request = buildMethod.invoke(requestBuilder);

        Class<?> summaryGeneratingListenerClass = classLoader.loadClass("org.junit.platform.launcher.listeners.SummaryGeneratingListener");
        Object listener = summaryGeneratingListenerClass.getDeclaredConstructor().newInstance();

        Class<?> launcherClass = classLoader.loadClass("org.junit.platform.launcher.Launcher");
        Class<?> testExecutionListenerClass = classLoader.loadClass("org.junit.platform.launcher.TestExecutionListener");
        Method registerMethod = launcherClass.getMethod("registerTestExecutionListeners", Array.newInstance(testExecutionListenerClass, 0).getClass());

        Object listenerArray = Array.newInstance(testExecutionListenerClass, 1);
        Array.set(listenerArray, 0, listener);
        registerMethod.invoke(launcher, listenerArray);

        Class<?> requestClass = classLoader.loadClass("org.junit.platform.launcher.LauncherDiscoveryRequest");
        Method executeMethod = launcherClass.getMethod("execute", requestClass, Array.newInstance(testExecutionListenerClass, 0).getClass());

        Object executeListenersArray = Array.newInstance(testExecutionListenerClass, 1);
        Array.set(executeListenersArray, 0, listener);

        try {
            executeMethod.invoke(launcher, request, executeListenersArray);
        } catch (InvocationTargetException e) {
            e.getCause().printStackTrace();  // Muestra la causa real
            throw e;
        }

        Method getSummary = summaryGeneratingListenerClass.getMethod("getSummary");
        Class<?> testExecutionSummaryClass = classLoader.loadClass("org.junit.platform.launcher.listeners.TestExecutionSummary");
        Object summary = getSummary.invoke(listener);

        try {
            Method printTo = testExecutionSummaryClass.getMethod("printTo", java.io.PrintWriter.class);
            printTo.invoke(summary, new java.io.PrintWriter(System.out, true));
        } catch (NoSuchMethodException e) {
        }

        Method getFailures = testExecutionSummaryClass.getMethod("getFailures");
        List<?> failures = (List<?>) getFailures.invoke(summary);

        for (Object failure : failures) {
            Method toStringMethod = failure.getClass().getMethod("toString");
            System.out.println("- Failure: " + toStringMethod.invoke(failure));
            fail = true;
        }

        if (!fail) {
            System.out.println("Test: 0 failures");
        }

        // Contadores
        Method getTestsFoundCount = testExecutionSummaryClass.getMethod("getTestsFoundCount");
        Method getTestsSucceededCount = testExecutionSummaryClass.getMethod("getTestsSucceededCount");
        long totalTests = (long) getTestsFoundCount.invoke(summary);
        long successfulTests = (long) getTestsSucceededCount.invoke(summary);

        System.out.println("- Total tests: " + totalTests);
        System.out.println("- Successful tests: " + successfulTests);
        classLoader.close();
        return !fail;
    }
	
	public URLClassLoader getClassLoader(IProject project) {
		count++;
	    String[] classPathEntries = null;
	    try {
	        project.open(null);
	        IJavaProject javaProject = JavaCore.create(project);
	        classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    }

	    List<URL> urlList = new ArrayList<URL>();
	    for (String entry : classPathEntries) {
	        IPath path = new Path(entry);
	        URL url;
	        try {
	            url = path.toFile().toURI().toURL();
	            urlList.add(url);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    for (URL url : urlList) {
	    	System.out.println("**urlList: "+url.getPath());
		}
	    ClassLoader parentClassLoader = project.getClass().getClassLoader();
	    URL[] urls = urlList.toArray(new URL[urlList.size()]);
	    
	    return new URLClassLoader(urls, JupiterTestEngine.class.getClassLoader());
	}

	public String findJUnitJarPath(IProject project) {
	    try {
	        IJavaProject javaProject = JavaCore.create(project);
	        String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
	        for (String entry : classPathEntries) {
	        	System.out.println("-classPathEntries: "+ entry);
	            if (entry.contains("junit") && entry.endsWith(".jar")) {
	            	System.out.println("- junit * jar: "+ entry);
	                return entry;
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null; 
	}

	public String extractClassName(String absolutePath) {
        if (absolutePath == null || !absolutePath.endsWith(".java")) {
            return null;
        }
        String fileName = absolutePath.substring(absolutePath.lastIndexOf(File.separatorChar) + 1);
        String className = fileName.substring(0, fileName.lastIndexOf('.'));
        
        return className;
    }

	public String extractPackageName(String pathSrc, String pathPackage) {
        java.nio.file.Path srcPath = Paths.get(pathSrc).toAbsolutePath();
        java.nio.file.Path packagePath = Paths.get(pathPackage).toAbsolutePath();
        if (!packagePath.startsWith(srcPath)) {
            throw new IllegalArgumentException("Package not found into src");
        }
        java.nio.file.Path relativePath = srcPath.relativize(packagePath);

        String packageName = relativePath.toString().replace(Path.SEPARATOR, '.');

        return packageName;
    }
	
	public String extractClassNameFromPath(String pathSrc, String pathClass) {
        if (!pathSrc.endsWith(File.separator)) {
            pathSrc += File.separator;
        }
        String relativePath = pathClass.replace(pathSrc, "");
        String classNameWithPackage = relativePath.replace(File.separatorChar, '.').substring(0, relativePath.lastIndexOf('.'));
        return classNameWithPackage;
    }
	
	public static String findRootPathInLargePath(List<String> rootPaths, String largePath) {
	    if (largePath == null || rootPaths == null) {
	        return null;
	    }

	    for (String rootPath : rootPaths) {
	        if (largePath.contains(rootPath)) {
	            return rootPath;
	        }
	    }
	    return null;
	}
	
}
