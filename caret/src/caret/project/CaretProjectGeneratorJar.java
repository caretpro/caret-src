package caret.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;
import org.eclipse.ui.PlatformUI;

/**
 * Generates and automatically builds Caret-branded Feature and Update Site projects.
 * This version automates the "Build All" process to generate JARs and metadata.
 */
@SuppressWarnings("restriction")
public class CaretProjectGeneratorJar {

    private static final String CARET_FEATURE_NATURE = "caret.feature.nature";
    private static final String CARET_SITE_NATURE = "caret.site.nature";
    private static final String PDE_FEATURE_NATURE = "org.eclipse.pde.FeatureNature";
    private static final String PDE_SITE_NATURE = "org.eclipse.pde.UpdateSiteNature";

    public void execute(IProject pluginProject) {
        IProgressMonitor monitor = new NullProgressMonitor();
        try {
            String pluginId = pluginProject.getName();
            String featureId = pluginId + ".feature";
            String siteProjectName = pluginId + ".site";
            
            // 1. Create Feature Project
            IProject featureProj = createBasicProject(featureId, monitor);
            addNatures(featureProj, new String[]{PDE_FEATURE_NATURE, CARET_FEATURE_NATURE}, monitor);
            createFeatureXml(featureProj, pluginId, monitor);
            createBuildProperties(featureProj, monitor);
            
            // 2. Create Site Project
            IProject siteProj = createBasicProject(siteProjectName, monitor);
            addNatures(siteProj, new String[]{PDE_SITE_NATURE, CARET_SITE_NATURE}, monitor);
            createSiteXml(siteProj, featureProj.getName(), monitor);

            // 3. Refresh and Build Workspace to ensure PDE models are ready
            featureProj.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            siteProj.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            
            // 4. Trigger the "Build All" equivalent (Site Export)
            runBuildAll(siteProj, featureId, monitor);

            System.out.println("Caret Update Site built successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates the "Build All" button by running the SiteExportOperation.
     */
    private void runBuildAll(IProject siteProject, String featureId, IProgressMonitor monitor) {
        try {
            IFile siteFile = siteProject.getFile("site.xml");
            WorkspaceSiteModel model = new WorkspaceSiteModel(siteFile);
            model.load();

            // SiteExportOperation handles the generation of features/, plugins/, 
            // content.jar, and artifacts.jar
            Class<?> clazz = Class.forName("org.eclipse.pde.internal.core.exports.SiteExportOperation");
            
            // Constructor: (ISiteModel[] models, String destination, String jobName)
            Constructor<?> constructor = clazz.getConstructor(ISiteModel[].class, String.class, String.class);

            // We export to the project's own location
            String destination = siteProject.getLocation().toOSString();
            
            Object op = constructor.newInstance(
                new ISiteModel[] { model }, 
                destination, 
                "Caret Build All: " + siteProject.getName()
            );

            // Run the operation via the Workbench Progress Service
            PlatformUI.getWorkbench().getProgressService().run(true, true, (org.eclipse.jface.operation.IRunnableWithProgress) op);
            
            // Refresh to see the new files (content.jar, artifacts.jar, etc.)
            siteProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            
            // Sync site.xml with the newly generated JAR version
            syncSiteXmlWithJars(siteProject, featureId, monitor);

        } catch (Exception e) {
            System.err.println("Failed to automate 'Build All': " + e.getMessage());
        }
    }

    private void syncSiteXmlWithJars(IProject siteProject, String featureId, IProgressMonitor monitor) throws Exception {
        IFolder featuresFolder = siteProject.getFolder("features");
        File featuresDir = featuresFolder.getLocation().toFile();
        
        if (featuresDir.exists() && featuresDir.isDirectory()) {
            File[] jars = featuresDir.listFiles((dir, name) -> name.startsWith(featureId) && name.endsWith(".jar"));
            
            if (jars != null && jars.length > 0) {
                File latestJar = jars[0];
                for (File f : jars) {
                    if (f.lastModified() > latestJar.lastModified()) latestJar = f;
                }

                String jarName = latestJar.getName();
                String finalVersion = jarName.substring(featureId.length() + 1, jarName.length() - 4);

                updateSiteXmlFinal(siteProject, featureId, jarName, finalVersion, monitor);
            }
        }
    }

    private void updateSiteXmlFinal(IProject project, String featureId, String jarName, String version, IProgressMonitor monitor) throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<site>\n" +
                "   <feature url=\"features/" + jarName + "\" id=\"" + featureId + "\" version=\"" + version + "\">\n" +
                "      <category name=\"caret-tools\"/>\n" +
                "   </feature>\n" +
                "   <category-def name=\"caret-tools\" label=\"Caret Extensions\"/>\n" +
                "</site>";
        
        writeFile(project, "site.xml", content, monitor);
    }

    // --- Helper Methods ---

    private IProject createBasicProject(String name, IProgressMonitor monitor) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        if (!project.exists()) {
            project.create(monitor);
        }
        if (!project.isOpen()) {
            project.open(monitor);
        }
        return project;
    }

    private void addNatures(IProject project, String[] natureIds, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        String[] newNatures = new String[prevNatures.length + natureIds.length];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        for (int i = 0; i < natureIds.length; i++) {
            newNatures[prevNatures.length + i] = natureIds[i];
        }
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }

    private void createFeatureXml(IProject project, String pluginId, IProgressMonitor monitor) throws CoreException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<feature id=\"" + project.getName() + "\" label=\"Caret Feature\" version=\"1.0.0.qualifier\" provider-name=\"Caret Tools\">\n" +
                "   <plugin id=\"" + pluginId + "\" version=\"0.0.0\"/>\n" +
                "</feature>";
        writeFile(project, "feature.xml", content, monitor);
    }

    private void createSiteXml(IProject project, String featureId, IProgressMonitor monitor) throws CoreException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<site>\n" +
                "   <feature url=\"features/" + featureId + "_1.0.0.qualifier.jar\" id=\"" + featureId + "\" version=\"1.0.0.qualifier\">\n" +
                "      <category name=\"caret-tools\"/>\n" +
                "   </feature>\n" +
                "   <category-def name=\"caret-tools\" label=\"Caret Extensions\"/>\n" +
                "</site>";
        writeFile(project, "site.xml", content, monitor);
    }

    private void createBuildProperties(IProject project, IProgressMonitor monitor) throws CoreException {
        writeFile(project, "build.properties", "bin.includes = feature.xml", monitor);
    }

    private void writeFile(IProject project, String fileName, String content, IProgressMonitor monitor) throws CoreException {
        IFile file = project.getFile(fileName);
        InputStream source = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        if (file.exists()) {
            file.setContents(source, true, true, monitor);
        } else {
            file.create(source, true, monitor);
        }
    }
}