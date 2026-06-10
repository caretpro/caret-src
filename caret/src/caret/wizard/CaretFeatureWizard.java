package caret.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.pde.internal.ui.wizards.feature.NewFeatureProjectWizard;
import org.eclipse.pde.internal.ui.wizards.feature.FeatureSpecPage;

/**
 * Custom wizard for creating Caret Feature Projects.
 * Extends the standard PDE Feature Wizard to inject custom Natures.
 */
@SuppressWarnings("restriction")
public class CaretFeatureWizard extends NewFeatureProjectWizard {

    @Override
    public boolean performFinish() {
        // 1. Execute the standard PDE feature creation logic
        boolean success = super.performFinish();

        if (success) {
            IProject project = null;
            
            // 2. Locate the main page to retrieve the project handle
            for (IWizardPage page : getPages()) {
                if (page instanceof FeatureSpecPage) {
                    project = ((FeatureSpecPage) page).getProjectHandle();
                    break;
                }
            }

            if (project != null) {
                final IProject finalProject = project;
                
                // 3. Schedule a workspace job to configure the project after creation
                WorkspaceJob job = new WorkspaceJob("Configuring Caret Feature") {
                    @Override
                    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                        try {
                            addCaretNature(finalProject, monitor);
                            return Status.OK_STATUS;
                        } catch (Exception e) {
                            return new Status(IStatus.ERROR, "caret.wizard", 
                                "Failed to apply Caret Nature to feature project", e);
                        }
                    }
                };
                
                // Set rule to avoid workspace locking conflicts
                job.setRule(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot());
                job.schedule();
            }
        }
        return success;
    }

    /**
     * Programmatically adds the Caret Feature Nature to the project description.
     */
    private void addCaretNature(IProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        
        // This ID must match the one defined in plugin.xml
        String caretNatureId = "caret.feature.nature"; 

        // Check if nature is already present
        for (String nature : prevNatures) {
            if (nature.equals(caretNatureId)) return;
        }

        // Append new nature to the existing array
        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = caretNatureId;
        
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }
}