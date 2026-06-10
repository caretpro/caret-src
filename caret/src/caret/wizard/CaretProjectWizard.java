package caret.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.pde.internal.ui.wizards.plugin.NewPluginProjectWizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction") 
public class CaretProjectWizard extends NewPluginProjectWizard {

	private Text descriptionText;
    private String userDescription = "";
    
    @Override
    public boolean performFinish() {
        // 1. Ejecutamos la creación base del proyecto
        boolean success = super.performFinish();

        if (success) {
            IProject project = fMainPage.getProjectHandle();
            String description = descriptionText.getText();

            org.eclipse.core.resources.WorkspaceJob job = new org.eclipse.core.resources.WorkspaceJob("Configuring Caret Project") {
                @Override
                public org.eclipse.core.runtime.IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    try {
                        addCaretNature(project, monitor);

                        injectCaretDependencies(project, description, monitor);
                        
                        return org.eclipse.core.runtime.Status.OK_STATUS;
                    } catch (Exception e) {
                        return new org.eclipse.core.runtime.Status(IStatus.ERROR, "caret.wizard", "Error configuring project", e);
                    }
                }
            };
            job.setRule(org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot());
            job.schedule();
        }

        return success;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        Composite composite = (Composite) fMainPage.getControl();
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("Plugin Description:");
        
        descriptionText = new Text(composite, SWT.BORDER);
        descriptionText.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.CENTER, true, false));
        descriptionText.setMessage("Enter the plugin description here...");
    }
    
    private void injectCaretDependencies(IProject project, String description, IProgressMonitor monitor) throws CoreException {
    	monitor.beginTask("Adding Caret to the end of Require-Bundle...", 2);
    	
        IFile manifestFile = project.getFile("META-INF/MANIFEST.MF");

        if (manifestFile.exists()) {
            try {
                InputStream is = manifestFile.getContents();
                String content = new String(is.readAllBytes(), manifestFile.getCharset());
                is.close();

                if (content.contains("Require-Bundle:") && !content.contains("caret")) {
                    
                    String[] lines = content.split("\n");
                    StringBuilder newContent = new StringBuilder();
                    boolean inRequireBundle = false;
                    boolean injected = false;

                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];

                        if (inRequireBundle && !line.startsWith(" ") && !injected) {
                            newContent.append(" caret\n");
                            injected = true;
                        }

                        if (line.startsWith("Require-Bundle:")) {
                            inRequireBundle = true;
                            line = line.endsWith(",") ? line : line + ",";
                        } else if (inRequireBundle && line.startsWith(" ")) {
                            if (!line.endsWith(",")) {
                                line = line + ",";
                            }
                        }
                        
                        if (line.startsWith("Bundle-Version:") && description != null && !description.isEmpty()) {
                            newContent.append("Bundle-Description: ").append(description).append("\n");
                        }

                        newContent.append(line).append("\n");
                    }
                    
                    if (!injected && inRequireBundle) {
                        newContent.append(" caret\n");
                    }

                    InputStream source = new ByteArrayInputStream(newContent.toString().getBytes(manifestFile.getCharset()));
                    manifestFile.setContents(source, true, true, monitor);
                }
            } catch (Exception e) {
                throw new CoreException(new org.eclipse.core.runtime.Status(
                    org.eclipse.core.runtime.IStatus.ERROR, "caret.wizard", "Error modificando Manifest", e));
            }
        }
        
        monitor.worked(1);
        project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
        monitor.done();
    }
    
    private void addCaretNature(IProject project, IProgressMonitor monitor) throws CoreException {
        org.eclipse.core.resources.IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        
        String caretNatureId = "caret.nature"; 

        for (String nature : prevNatures) {
            if (nature.equals(caretNatureId)) return;
        }

        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = caretNatureId;
        
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }
}