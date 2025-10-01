package caret.project.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import caret.tool.Parser;
import caret.tool.Util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class JavaProject {

	private IWorkspace workspace;
	private String projectName;
	private IWorkspaceRoot root;
	private IProject project;
	public static String PACKAGE_DEFAULT ="";
	public static String CONTENT_DEFAULT ="";
	private int indexMethods=0;
	
	public IProject getProject() {
		return project;
	}

	public void setProject(String projectName) {
		workspace = ResourcesPlugin.getWorkspace();
        root = workspace.getRoot();
        IProject[] projects = root.getProjects();
        for (IProject proj : projects) {
        	String name = proj.getName().toString();
        	if(name.equals(projectName)) {
                this.project = proj;
                this.projectName = projectName;
                return;
        	}
        	
        }
	}
	
	public IProject findProject(String projectName) {
		workspace = ResourcesPlugin.getWorkspace();
        root = workspace.getRoot();
        IProject[] projects = root.getProjects();
        for (IProject proj : projects) {
        	String name = proj.getName().toString();
        	if(name.equals(projectName)) {
                this.project = proj;
                this.projectName = projectName;
                return project;
        	}
        }
        return null;
	}

	public JavaProject() {
		// TODO Auto-generated constructor stub
	}
	
	public JavaProject(IWorkspace workspace) {
		// TODO Auto-generated constructor stub
		workspace = ResourcesPlugin.getWorkspace();
    	root= workspace.getRoot();
	}
	
	public JavaProject(IWorkspace workspace, String projectName) {
		// TODO Auto-generated constructor stub
		workspace = ResourcesPlugin.getWorkspace();
		this.projectName = projectName;
    	root= workspace.getRoot();
	}

	public IWorkspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(IWorkspace workspace) {
		this.workspace = workspace;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public boolean existsProject(String projectName) {
		if(root.getProject(projectName).exists()) {
			return true;
		}else {
			return false;
		}
	}
	
	public static IProject getProject(String projectName) {
		IProject project = null;
		try {
			if(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).exists()) {
				project= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if(project != null) {
					return project;
				}else {
					return null;
				}
			}else {
				return null;
			}
		} catch (Exception e) {
			System.out.println("#Error creating new project");
			return null;
		}
	}
	
	public boolean createProject() {
		try {
			project= root.getProject(projectName);
			project.create(null);
	    	project.open(null);
	    	IProjectDescription desc = project.getDescription();
	    	desc.setNatureIds(new String[] {JavaCore.NATURE_ID});
	    	project.setDescription(desc, null);
	    	IJavaProject javaProject = JavaCore.create(project);
	    	IFolder binDir = project.getFolder("bin");
	    	IFolder srcDir = project.getFolder("src");
	    	srcDir.create(false, false, null);
	    	javaProject.setOutputLocation(binDir.getFullPath(), null);
	    	IPath container = new Path(getDefaultJREContainerPath());
	    	//IPath container = new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.launching.macosx.MacOSXType/Java SE 17.0.1 [17.0.1]");
	    	IClasspathEntry cpeCon= JavaCore.newContainerEntry(container, null, new IClasspathAttribute[] { JavaCore.newClasspathAttribute("module", "true")}, false);
	    	IClasspathEntry cpeSrc= JavaCore.newSourceEntry(srcDir.getFullPath());
	    	javaProject.setRawClasspath(new IClasspathEntry[] {cpeCon,cpeSrc}, null);
	    	return true;
    	} catch (Exception e) {
    		System.out.println("ERROR JAVAPROJECT: "+ e.getMessage());
    		return false;
		}
	} 
	
	public String getDefaultJREContainerPath() {
    	IVMInstall jre = JavaRuntime.getDefaultVMInstall();
        IClasspathEntry cpeJre = JavaRuntime.getDefaultJREContainerEntry();
        return cpeJre.getPath()+"/"+jre.getVMInstallType().getId()+"/"+jre.getName();
    }
	
	
	public String createClass(String className, String content, String packageName){
        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                IPackageFragment[] packages = javaProject.getPackageFragments();
                for(IPackageFragmentRoot packageFragmentRoot: javaProject.getPackageFragmentRoots()) {
                	if(packageFragmentRoot.getKind()==IPackageFragmentRoot.K_SOURCE){
                		StringBuffer buffer = new StringBuffer();
                		//System.out.println("SRC SOURCE:"+packageFragmentRoot.toString());
                		if(packageName == null) {
                			packageName = PACKAGE_DEFAULT;
                		}
                		if(packageName != PACKAGE_DEFAULT) {
                			IPackageFragment pack = findPackage(project.getName(), packageName);
                    		if(pack == null) {
                    			pack = packageFragmentRoot.createPackageFragment(packageName, true, null);
                    			if(pack != null) {
                    				buffer.append("package " + pack.getElementName() + ";\n");
                    			}else {
                    				System.out.println("Error creating package " + pack.getElementName() + ";\n");
                    			}
                    		}
                		}
                    	if (content == CONTENT_DEFAULT) {
                        	buffer.append("public class "+className+" {\n\n}");
						}else {
							buffer.append(content);
						}
                    	//ICompilationUnit cu = pack.createCompilationUnit(className+".java", buffer.toString(), false, null);
                    	ICompilationUnit compilationUnit = getICompilationUnit(className, packageFragmentRoot.getPackageFragment(packageName));
                    	if(compilationUnit == null){
                    		compilationUnit = packageFragmentRoot.getPackageFragment(packageName).createCompilationUnit(className+".java", buffer.toString(), false, null);
                    		if (compilationUnit != null) {
                		    	return compilationUnit.getResource().getProjectRelativePath().toString();
                            }else {
                            	System.out.println("##2 CREATE CLASS CU ULL"+compilationUnit.getElementName());
                            }
                    	}else {
                		    ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
                		    
                		    // Modify buffer and reconcile
                		    IBuffer iBuffer = ((IOpenable)workingCopy).getBuffer();
                		    iBuffer.setContents(content);
                		    workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
                		    
                		    // Commit changes
                		    workingCopy.commitWorkingCopy(false, null);
                		    
                		    // Destroy working copy
                		    workingCopy.discardWorkingCopy();
                		    if (compilationUnit != null) {
                		    	return compilationUnit.getResource().getProjectRelativePath().toString();
                            }else {
                            	System.out.println("##2 CREATE CLASS CU ULL"+compilationUnit.getElementName());
                            }
                    	}
                	}
            	}
            }
        	
        } catch (CoreException e) {
        	System.out.println("#ERROR JAVAPROJECT: "+ e.getMessage());
        }
        return null;
    }
	
	public ICompilationUnit createClass(IProject project, String className, String content, String packageName){
		ICompilationUnit compilationUnit = null;
		try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                IPackageFragment[] packages = javaProject.getPackageFragments();
                for(IPackageFragmentRoot packageFragmentRoot: javaProject.getPackageFragmentRoots()) {
                	if(packageFragmentRoot.getKind()==IPackageFragmentRoot.K_SOURCE){
                		StringBuffer buffer = new StringBuffer();
                		//System.out.println("SRC SOURCE:"+packageFragmentRoot.toString());
                		if(packageName == null) {
                			packageName = PACKAGE_DEFAULT;
                		}
                		if(packageName != PACKAGE_DEFAULT) {
                			IPackageFragment pack = findPackage(project.getName(), packageName);
                    		if(pack == null) {
                    			pack = packageFragmentRoot.createPackageFragment(packageName, true, null);
                    			if(pack != null) {
                    				buffer.append("package " + pack.getElementName() + ";\n");
                    			}else {
                    				System.out.println("Error creating package " + pack.getElementName() + ";\n");
                    			}
                    		}
                		}
                    	if (content == CONTENT_DEFAULT) {
                        	buffer.append("public class "+className+" {\n\n}");
						}else {
							buffer.append(content);
						}
                    	//ICompilationUnit cu = pack.createCompilationUnit(className+".java", buffer.toString(), false, null);
                    	compilationUnit = getICompilationUnit(className, packageFragmentRoot.getPackageFragment(packageName));
                    	if(compilationUnit == null){
                    		compilationUnit = packageFragmentRoot.getPackageFragment(packageName).createCompilationUnit(className+".java", buffer.toString(), false, null);
                    	}else {
                    		// Create working copy
                    		System.out.println("##Actualizando CU:"+compilationUnit.getElementName());
                		    ICompilationUnit workingCopy = compilationUnit.getWorkingCopy(null);
                		    
                		    // Modify buffer and reconcile
                		    IBuffer iBuffer = ((IOpenable)workingCopy).getBuffer();
                		    iBuffer.setContents(content);
                		    workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
                		    
                		    // Commit changes
                		    workingCopy.commitWorkingCopy(false, null);
                		    
                		    // Destroy working copy
                		    workingCopy.discardWorkingCopy();
                    	}
                	}
            	}
            }
        	
        } catch (CoreException e) {
        	System.out.println("#JavaProject.createClass ERROR: "+ e.getMessage());
        	return null;
        }
		return compilationUnit;
    }
	
	public void createPackage(String packageName){
        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                IPackageFragment[] packages = javaProject.getPackageFragments();
                for(IPackageFragmentRoot packageFragmentRoot: javaProject.getPackageFragmentRoots()) {
                	if(packageFragmentRoot.getKind()==IPackageFragmentRoot.K_SOURCE){
                		//System.out.println("SRC SOURCE:"+packageFragmentRoot.toString());
                		IPackageFragment pack = packageFragmentRoot.createPackageFragment(packageName, true, null);
                	}
                	
            	}
            }
        	
        } catch (CoreException e) {
        	System.out.println("#ERROR JAVAPROJECT: "+ e.getMessage());
        }
    }
	
	public static void createPackageWithoutCheck(String projectName, String packageName){
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = workspaceRoot.getProject(projectName);
        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                IPackageFragment[] packages = javaProject.getPackageFragments();
                for(IPackageFragmentRoot packageFragmentRoot: javaProject.getPackageFragmentRoots()) {
                	if(packageFragmentRoot.getKind()==IPackageFragmentRoot.K_SOURCE){
                		System.out.println("SRC SOURCE:"+packageFragmentRoot.toString());
                		IPackageFragment pack = packageFragmentRoot.createPackageFragment(packageName, true, null);
                	}
                	
            	}
            }
        	
        } catch (CoreException e) {
        	System.out.println("#ERROR JAVAPROJECT: "+ e.getMessage());
        }
    }

	public static IPackageFragment createPackage(String projectName, String packageName) {
        IPackageFragment pack = null;
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = workspaceRoot.getProject(projectName);

        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                pack = findPackage(projectName, packageName);
                if (pack != null) {
                    System.out.println("Package already exists: " + packageName);
                    return pack; 
                }
                for (IPackageFragmentRoot packageFragmentRoot : javaProject.getPackageFragmentRoots()) {
                    if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        System.out.println("Creating package in source folder: " + packageFragmentRoot.toString());
                        pack = packageFragmentRoot.createPackageFragment(packageName, true, null);
                        return pack;
                    }
                }
            }
        } catch (CoreException e) {
            System.err.println("Error while handling Java project: " + e.getMessage());
        }

        return pack;
    }
	
	public static IPackageFragment findPackage(String projectName, String packageName) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = workspaceRoot.getProject(projectName);

        try {
            if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);

                // Search for the package
                for (IPackageFragment pack : javaProject.getPackageFragments()) {
                    if (pack.getElementName().equals(packageName) && pack.exists()) {
                        return pack; // Package found
                    }
                }
            }
        } catch (CoreException e) {
            System.err.println("Error while searching for package: " + e.getMessage());
        }

        return null; // Package not found
    }
	
	public void createInterface(String className, String content, String packageName){
		if (content == CONTENT_DEFAULT) {
        	content = "public interface "+className+" {\n\n}";
		}
		createClass(className, content, packageName);
    }
    
    private ICompilationUnit getICompilationUnit(String name, IPackageFragment packageFragment){
    	ICompilationUnit compilationUnit = null;
    	try {
            for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
                if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(name)) {
                	//System.out.println("***ClassName igual: " + name+"=="+Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME));
                	compilationUnit = unit;
                	return compilationUnit;
                }else {
                	//System.out.println("***ClassName diferente: " + name+"=="+Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME));
                }
            }
		} catch (Exception e) {
			System.out.println("#ERROR - getICompilationUnit: " + e.getMessage());
		}
    	return compilationUnit;
    }
    
    public static String getClassSource(IProject project, String className) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
			    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
			        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			        	//System.out.println("*** ElementName: "+unit.getElementName());
			        	if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(className)) {
				            String text = unit.getSource();
				            /*text = text.replace("\n", "");
				            text = text.replace("\"", "\\\"");
				            text = text.replace("\'", "\\\'");*/
				            return text;
			        	}
			        }
			    }
			}
		} catch (Exception e) {
			System.out.println("ERROR GET SOURCE" + e.getMessage());
		}
		return null;
	}
    
    public static String getClassSource(IResource resource) {
    	ICompilationUnit unit = getClass(resource);
    	if(unit == null) {
    		return null;
    	}else {
    		try {
				return unit.getSource();
			} catch (JavaModelException e) {
				e.printStackTrace();
				return null;
			}
    	}
	}
    
    public static String getClassPackage(IProject project, String className) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
			    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
			        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			        	System.out.println("*** ElementName: "+unit.getElementName());
			        	if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(className)) {
				            return mypackage.getElementName();
			        	}
			        }
			    }
			}
		} catch (Exception e) {
			System.out.println("ERROR GET PACKAGE" + e.getMessage());
		}
		return null;
	}
    
    public static ICompilationUnit getClass(IProject project, String className) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
			    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
			        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			        	System.out.println("*** ElementName: "+unit.getElementName());
			        	System.out.println("*** ElementName parseado: "+Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME));
			        	if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(className)) {
				            return unit;
			        	}
			        }
			    }
			}
		} catch (Exception e) {
			System.out.println("ERROR GET SOURCE" + e.getMessage());
		}
		return null;
	}
    
    public static ICompilationUnit getClass(IResource resource) {
    	IProject project = resource.getProject();
    	if(project == null) {
    		return null;
    	}
    	String className = Util.getClassName(resource.getName());
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
			    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
			        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			        	System.out.println("*** ElementName: "+unit.getElementName());
			        	System.out.println("*** ElementName parseado: "+Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME));
			        	if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(className)) {
				            return unit;
			        	}
			        }
			    }
			}
		} catch (Exception e) {
			System.out.println("ERROR GET SOURCE" + e.getMessage());
		}
		return null;
	}
    
    public static IMethod getMethod(IProject project, String className, String methodName) {
    	System.out.println("*** getMethod: ClassName: "+className+", MethodName:"+methodName);
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
			    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
			        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			        	if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(className)) {
			        		System.out.println("*** ClassName Igual: "+unit.getElementName()+":"+className);
				            String text = unit.getSource();
				            IType[] allTypes = unit.getAllTypes();
				    	    for (IType type : allTypes) {
				    	    	IMethod[] methods = type.getMethods();
				    	    	for (IMethod method : methods) {
				    	    		if(method.getElementName().equals(methodName)) {
				    	    			System.out.println("*** MethodName Igual: "+method.getElementName()+":"+methodName);
				    	    			return method;
				    	    		}else {
				    	    			System.out.println("*** MethodName Diferente: "+method.getElementName()+":"+methodName);
				    	    		}
				    	    	}
				    	    }
			        	}else {
			        		System.out.println("*** ClassName Diferentes: "+unit.getElementName()+":"+className);
			        	}
			        }
			    }
			}
		} catch (Exception e) {
			System.out.println("ERROR GET SOURCE" + e.getMessage());
		}
		return null;
	}

	public static boolean createMethod(IProject project, String className, String methodContent) {
		System.out.println("###############CREATED METHOD#############");
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			for (IPackageFragment mypackage : packages) {
			    if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
			        for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			        	if(Parser.getClassName(unit.getElementName(), Parser.TYPE_FILENAME).equals(className)) {
			        		IProgressMonitor monitor = new NullProgressMonitor();
				            IMethod method = unit.getType(className).createMethod(methodContent, null, true, monitor);
				            if(method!=null) {
				            	System.out.println("*** Created method: "+method.getElementName());
				            	return true;
				            }else {
				            	System.out.println("*** Not created method");
				            	return false;
				            	
				            }
			        	}
			        }
			    }
			}
		} catch (Exception e) {
			System.out.println("#Error - createMethod:" + e.getMessage());
		}
		return false;
	}
	
	public static String getPathSource(IProject project) {
        try {
			if (project != null && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
			    IJavaProject javaProject = JavaCore.create(project);
			    try {
			        IClasspathEntry[] entries = javaProject.getRawClasspath();
			        for (IClasspathEntry entry : entries) {
			            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
			                IPath path = entry.getPath();
			                IPath fullPath = project.getWorkspace().getRoot().getFolder(path).getLocation();
			                return fullPath.toString();
			            }
			        }
			    } catch (JavaModelException e) {
			        e.printStackTrace();
			    }
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
        return null;
    }
	
	public static List<String> getSourcePaths(IProject project) {
    	List<String> srcPaths = new ArrayList<>();
        try {
			if (project != null && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
			    IJavaProject javaProject = JavaCore.create(project);
		        IClasspathEntry[] entries = javaProject.getRawClasspath();
		        for (IClasspathEntry entry : entries) {
		            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
		                IPath path = entry.getPath();
		                IPath fullPath = project.getWorkspace().getRoot().getFolder(path).getLocation();
		                srcPaths.add(fullPath.toString()) ;
		            }
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        return srcPaths;
    }
	
	public static String findSelectedMethodName(ICompilationUnit compilationUnit, ITextSelection selection) {
        // Parse the compilation unit using ASTParser
        ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
        parser.setSource(compilationUnit);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        // Find the selected node based on the selection offset
        ASTNode selectedNode = NodeFinder.perform(astRoot, selection.getOffset(), selection.getLength());
        
        // Traverse up to find the enclosing MethodDeclaration
        ASTNode current = selectedNode;
        while (current != null && !(current instanceof MethodDeclaration)) {
            current = current.getParent();
        }

        if (current instanceof MethodDeclaration) {
            MethodDeclaration method = (MethodDeclaration) current;
            return method.getName().getIdentifier();
        }

        return null; // No method found
    }
	
	public static MethodDeclaration findSelectedMethod(IEditorPart editorPart, ITextSelection textSelection) {
        if (!(editorPart instanceof ITextEditor)) {
            return null; // Editor no compatible
        }

        ITextEditor textEditor = (ITextEditor) editorPart;
        ICompilationUnit compilationUnit = JavaUI.getWorkingCopyManager().getWorkingCopy(textEditor.getEditorInput());
        if (compilationUnit == null) {
            return null; 
        }

        ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
        parser.setSource(compilationUnit);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

        ASTNode selectedNode = NodeFinder.perform(astRoot, textSelection.getOffset(), textSelection.getLength());

        ASTNode current = selectedNode;
        while (current != null && !(current instanceof MethodDeclaration)) {
            current = current.getParent();
        }

        if (current instanceof MethodDeclaration) {
            return (MethodDeclaration) current;
        }

        return null; 
    }
	
	public static String replaceMethodBody(String sourceCode, String methodName, String newMethodCode) {
        // Parse the source code
        ASTParser parser = ASTParser.newParser(AST.JLS17); // Adjust JLS version if needed
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(sourceCode.toCharArray());
        parser.setResolveBindings(false); // Set true if working in Eclipse project context

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        AST ast = cu.getAST();
        ASTRewrite rewriter = ASTRewrite.create(ast);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration method) {
                if (method.getName().getIdentifier().equals(methodName)) {
                    // Parse the new method body
                    Block newBody = ast.newBlock();
                    Statement statement = (Statement) rewriter.createStringPlaceholder(newMethodCode, ASTNode.RETURN_STATEMENT);
                    newBody.statements().add(statement);

                    // Replace the existing body
                    rewriter.set(method, MethodDeclaration.BODY_PROPERTY, newBody, null);
                }
                return super.visit(method);
            }
        });

        // Apply changes to the source code
        Document document = new Document(sourceCode);
        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return document.get();
    }
	
	private void openClassInEditor(String projectName, String classFilePath) {
	    try {
	        // Get the workspace project
	        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	        if (project != null && project.isAccessible()) {
	            // Get the file within the project
	            IFile file = project.getFile(new Path(classFilePath));
	            if (file.exists()) {
	                // Open the file in the default editor
	                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	                IDE.openEditor(page, file);
	                System.out.println("### File opened in editor: " + file.getFullPath());
	            } else {
	                System.out.println("### File not found: " + classFilePath);
	            }
	        } else {
	            System.out.println("### Project not accessible: " + projectName);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.out.println("### Error opening file in editor: " + e.getMessage());
	    }
	}
	
	public static String getJavaVersion(IProject project) {
        try {
            if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject javaProject = JavaCore.create(project);
                
                // get compiler version
                String compliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
                return compliance;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	
}
