package caret.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import caret.project.CaretProjectGenerator;
import caret.project.CaretProjectGeneratorJar;
import caret.project.ProjectGenerator;
import caret.tool.Log;

public class CreateUSHandler extends AbstractHandler {
	
    /*@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
        IProject project = (IProject) ((IAdaptable) selection.getFirstElement()).getAdapter(IProject.class);
        Log.d("CreateUS:"+project.getName());
        ProjectGenerator pg = new ProjectGenerator();
        pg.execute(project);
    
        return null;
    }*/

	@Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
        Object firstElement = selection.getFirstElement();
        
        if (firstElement instanceof IAdaptable) {
            IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject.class);
            if (project != null) {
                Log.d("Generating Caret projects for: " + project.getName());
                
                // Use the new Caret-specific generator
                CaretProjectGenerator pg = new CaretProjectGenerator();
                pg.execute(project);
            }
        }
    
        return null;
    }

}