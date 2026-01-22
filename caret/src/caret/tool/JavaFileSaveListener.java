package caret.tool;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import caret.StatisticsView;


public class JavaFileSaveListener implements IResourceChangeListener {

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            try {
                event.getDelta().accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta delta) throws CoreException {
                        IResource resource = delta.getResource();
                        if (resource != null && resource.getType() == IResource.FILE) {
                            if ("java".equals(resource.getFileExtension())
                                && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                            	if(StatisticsView.getInstance()!=null) {
                            		StatisticsView.getInstance().updateStatistics();
                            	}
                            }
                        }
                        return true;
                    }
                });
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }
}
