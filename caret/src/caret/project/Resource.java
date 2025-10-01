package caret.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import caret.tool.Parser;

public class Resource {
	
    public static final int STRUCTURED_SELECTION = 1;
    public static final int TEXT_SELECTION = 1;
    IResource iResource = null;
    
    public Resource(IResource iResource) {
    	this.iResource = iResource;
    }
	
	public static IResource getSelectedResource(int selectionType) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    	ISelection selection = (ISelection)page.getSelection();
    	//System.out.println("#selection:"+selection.toString());
    	if(selectionType == STRUCTURED_SELECTION) {
    		if (selection instanceof IStructuredSelection) {
            	IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                Object firstElement = structuredSelection.getFirstElement();
                return (IResource)Platform.getAdapterManager().getAdapter(firstElement, IResource.class);
            }else {
            	return null;
            }
    	}
    	if(selectionType == TEXT_SELECTION) {
    		if (selection instanceof ITextSelection) {
        		ITextSelection textSelection = (ITextSelection) selection;
        		//System.out.println("#textSelection:"+textSelection.getText());
        		IEditorPart  editorPart = page.getActiveEditor();
        		if(editorPart  != null){
        		    return (IResource)editorPart.getEditorInput().getAdapter(IResource.class);
        		}else {
        			return null;
        		}
            }else {
            	return null;
            }
    	}
    	return null;
	}
	
	public static IResource getSelectedResource() {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    	ISelection selection = (ISelection)page.getSelection();
	    	if (selection instanceof IStructuredSelection) {
	        	IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	            Object firstElement = structuredSelection.getFirstElement();
	            return (IResource)Platform.getAdapterManager().getAdapter(firstElement, IResource.class);
	        }
	    	if (selection instanceof ITextSelection) {
	    		ITextSelection textSelection = (ITextSelection) selection;
	    		//System.out.println("#textSelection:"+textSelection.getText());
	    		IEditorPart  editorPart = page.getActiveEditor();
	    		if(editorPart  != null){
	    		    return (IResource)editorPart.getEditorInput().getAdapter(IResource.class);
	    		}
	        }
		} catch (Exception e) {
			System.out.println("###getSelectedResource() = null");
		}
		
    	return null;
	}
	
	public static boolean isIStructuredSelection() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    	ISelection selection = (ISelection)page.getSelection();
    	if (selection instanceof IStructuredSelection) {
        	return true;
        }
    	return false;
	}
	
	public static boolean isITextSelection() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    	ISelection selection = (ISelection)page.getSelection();
    	if (selection instanceof ITextSelection) {
    		return true;
        }
    	return false;
	}
	
	public static IProject getProject(IResource resource) {
		if(resource != null) {
			return resource.getProject();
		}else {
			return null;
		}
	}
	
	public boolean isICompilationUnit() {
		IJavaElement javaElement = JavaCore.create(iResource);
		if(javaElement != null) {
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAdapter(ICompilationUnit.class);
			if(cu != null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isClass() {
		IJavaElement javaElement = JavaCore.create(iResource);
		if(javaElement != null) {
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAdapter(ICompilationUnit.class);
			if(cu != null) {
				IType type = cu.getType(Parser.getClassName(javaElement.getElementName(), Parser.TYPE_FILENAME));
				if(type != null) {
					try {
						if(type.isClass()) {
							return true;
						}
					} catch (JavaModelException e) {
						return false;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isInterface() {
		IJavaElement javaElement = JavaCore.create(iResource);
		if(javaElement != null) {
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAdapter(ICompilationUnit.class);
			if(cu != null) {
				IType type = cu.getType(Parser.getClassName(javaElement.getElementName(), Parser.TYPE_FILENAME));
				if(type != null) {
					try {
						if(type.isInterface()) {
							return true;
						}
					} catch (JavaModelException e) {
						return false;
					}
				}
			}
		}
		return false;
	}
	
	public String getSource() {
		IJavaElement javaElement = JavaCore.create(iResource);
		if(javaElement != null) {
			ICompilationUnit cu = (ICompilationUnit) javaElement.getAdapter(ICompilationUnit.class);
			if(cu != null) {
				IType type = cu.getType(Parser.getClassName(javaElement.getElementName(), Parser.TYPE_FILENAME));
				if(type != null) {
					try {
						return type.getSource();
					} catch (JavaModelException e) {
						return null;
					}
				}
			}
		}
		return null;
	}
	
	public static IEditorPart getEditorPart() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}
	
	public static IResource getActiveResourceNonUIThread() {
        final IResource[] activeResource = new IResource[1];
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
            	try {
            		IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
                    if (input instanceof FileEditorInput) {
                    	activeResource[0] = ((FileEditorInput) input).getFile();
                    } else if (input instanceof IAdaptable) {
                    	activeResource[0] = ((IAdaptable) input).getAdapter(IResource.class);
                    }
				} catch (Exception e) {
					activeResource[0] = null;
					System.out.println("### getActiveResourceNonUIThread = null");
				}
            }
        });
        return activeResource[0];
    }


}
