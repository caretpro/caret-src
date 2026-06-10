package caret.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.internal.ui.wizards.site.NewSiteProjectWizard;
import org.eclipse.pde.internal.ui.wizards.site.NewSiteProjectCreationPage;

/**
 * Custom wizard for creating Caret Site Projects (Update Sites).
 * Extends the standard PDE Site Wizard to apply custom Natures.
 */
@SuppressWarnings("restriction")
public class CaretSiteWizard extends NewSiteProjectWizard {

    @Override
    public boolean performFinish() {
        // 1. Execute the standard PDE site creation logic
        boolean success = super.performFinish();

        if (success) {
            IProject project = null;
            
            // 2. Iterate through pages to find the Creation Page
            for (IWizardPage page : getPages()) {
                if (page instanceof NewSiteProjectCreationPage) {
                    // This class has the public method getProjectHandle()
                    project = ((NewSiteProjectCreationPage) page).getProjectHandle();
                    break;
                }
            }

            // 3. If project was found, apply the nature in a background job
            if (project != null) {
                final IProject finalProject = project;
                
                WorkspaceJob job = new WorkspaceJob("Configuring Caret Site") {
                    @Override
                    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                        try {
                            addCaretNature(finalProject, monitor);
                            return Status.OK_STATUS;
                        } catch (Exception e) {
                            return new Status(IStatus.ERROR, "caret.wizard", 
                                "Failed to apply Caret Nature to site project", e);
                        }
                    }
                };
                
                // Set the scheduling rule to the project or workspace root
                job.setRule(ResourcesPlugin.getWorkspace().getRoot());
                job.schedule();
            }
        }
        return success;
    }

    /**
     * Programmatically adds the Caret Site Nature to the project description.
     */
    private void addCaretNature(IProject project, IProgressMonitor monitor) throws CoreException {
        if (!project.isOpen()) {
            project.open(monitor);
        }
        
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        
        String caretNatureId = "caret.site.nature"; 

        // Check if nature is already present
        for (String nature : prevNatures) {
            if (nature.equals(caretNatureId)) return;
        }

        // Add the new nature
        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = caretNatureId;
        
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }
}