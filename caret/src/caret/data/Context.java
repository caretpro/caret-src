package caret.data;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;

public class Context {

	private Resource resource;
	
	public Context(IResource iResource, ITextSelection iTextSelection) {
		this.resource = new Resource(iResource, iTextSelection);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
}
