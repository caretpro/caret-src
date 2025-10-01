package caret.data;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;

public class Resource {
	String projectName;
	String fileName;
	String fullPath;
	String projectRelativePath;
	CodeFragment codeFragment;

	public Resource(IResource resource, ITextSelection iTextSelection) {
		setProjectName(resource.getProject().getName());
		setFileName(resource.getName());
		setFullPath(resource.getFullPath().toString());
		setProjectRelativePath(resource.getProjectRelativePath().toString());
		if(iTextSelection != null) {
			this.codeFragment = new CodeFragment (iTextSelection);
		}
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getProjectRelativePath() {
		return projectRelativePath;
	}

	public void setProjectRelativePath(String projectRelativePath) {
		this.projectRelativePath = projectRelativePath;
	}
	
	public void update(String newFileName) {
		String fileName = getFileName();
		setFileName(newFileName);
		setFullPath(getFullPath().replace(fileName, newFileName));
		setProjectRelativePath(getProjectRelativePath().replace(fileName, newFileName));	
	}
	
	public CodeFragment getCodeFragment() {
		return codeFragment;
	}

	public void setCodeFragment(CodeFragment codeFragment) {
		this.codeFragment = codeFragment;
	}
}
