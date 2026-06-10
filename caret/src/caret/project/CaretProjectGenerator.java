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
 * Generates Caret-branded Feature and Update Site projects based on a Plugin project.
 */
@SuppressWarnings("restriction")
public class CaretProjectGenerator {

    // Custom Nature IDs defined in plugin.xml
    private static final String CARET_FEATURE_NATURE = "caret.feature.nature";
    private static final String CARET_SITE_NATURE = "caret.site.nature";
    
    // Standard PDE Nature IDs
    private static final String PDE_FEATURE_NATURE = "org.eclipse.pde.FeatureNature";
    private static final String PDE_SITE_NATURE = "org.eclipse.pde.UpdateSiteNature";

    public void execute(IProject pluginProject) {
        IProgressMonitor monitor = new NullProgressMonitor();
        try {
            String pluginId = pluginProject.getName();
            String featureId = pluginId + ".feature";
            
            // 1. Create and configure Caret Feature Project
            IProject featureProj = createBasicProject(featureId, monitor);
            // We add both Standard PDE and Custom Caret natures
            addNatures(featureProj, new String[]{PDE_FEATURE_NATURE, CARET_FEATURE_NATURE}, monitor);
            createFeatureXml(featureProj, pluginId, monitor);
            createBuildProperties(featureProj, monitor);
            
            // 2. Create and configure Caret Site Project
            IProject siteProj = createBasicProject(pluginId + ".site", monitor);
            // We add both Standard PDE and Custom Caret natures
            addNatures(siteProj, new String[]{PDE_SITE_NATURE, CARET_SITE_NATURE}, monitor);
            createSiteXml(siteProj, featureProj.getName(), monitor);

            System.out.println("Caret Projects generated successfully.");
            
            featureProj.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            siteProj.refreshLocal(IProject.DEPTH_INFINITE, monitor);

            // 3. Perform build and sync
            buildAndSyncUpdateSite(siteProj, featureId);

        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    private IProject createBasicProject(String name, IProgressMonitor monitor) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        if (!project.exists()) {
            project.create(monitor);
            project.open(monitor);
        }
        return project;
    }

    /**
     * Adds multiple natures to the project description.
     */
    private void addNatures(IProject project, String[] natureIds, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        
        // Calculate new size and merge
        String[] newNatures = new String[prevNatures.length + natureIds.length];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        
        for (int i = 0; i < natureIds.length; i++) {
            newNatures[prevNatures.length + i] = natureIds[i];
        }
        
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }

    private void createFeatureXml(IProject project, String pluginId, IProgressMonitor monitor) throws CoreException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<feature\n");
        sb.append("      id=\"").append(project.getName()).append("\"\n");
        sb.append("      label=\"Caret Generated Feature\"\n");
        sb.append("      version=\"1.0.0.qualifier\"\n");
        sb.append("      provider-name=\"Caret Tools\">\n\n");
        sb.append("   <plugin\n");
        sb.append("         id=\"").append(pluginId).append("\"\n");
        sb.append("         version=\"0.0.0\"/>\n\n");
        sb.append("</feature>");
        
        writeFile(project, "feature.xml", sb.toString(), monitor);
    }

    private void createSiteXml(IProject project, String featureId, IProgressMonitor monitor) throws CoreException {
        String fullVersion = "1.0.0.qualifier";
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<site>\n");
        sb.append("   <feature url=\"features/").append(featureId).append("_").append(fullVersion).append(".jar\" ");
        sb.append("id=\"").append(featureId).append("\" ");
        sb.append("version=\"").append(fullVersion).append("\">\n");
        sb.append("      <category name=\"caret-tools\"/>\n");
        sb.append("   </feature>\n");
        sb.append("   <category-def name=\"caret-tools\" label=\"Caret Extensions\"/>\n");
        sb.append("</site>");
        
        writeFile(project, "site.xml", sb.toString(), monitor);
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

    /**
     * Uses PDE internal export operations to build the site and sync the XML versions.
     */
    public void buildAndSyncUpdateSite(IProject siteProject, String featureId) {
        try {
            IFile siteFile = siteProject.getFile("site.xml");
            if (!siteFile.exists()) return;

            WorkspaceSiteModel model = new WorkspaceSiteModel(siteFile);
            model.load();

            // Invoke PDE Internal Site Export Operation
            try {
                Class<?> clazz = Class.forName("org.eclipse.pde.internal.core.exports.SiteExportOperation");
                Constructor<?> constructor = clazz.getConstructor(ISiteModel[].class, String.class, String.class);

                Object op = constructor.newInstance(
                    new ISiteModel[] { model }, 
                    siteProject.getLocation().toOSString(), 
                    "Caret: Building Update Site"
                );

                PlatformUI.getWorkbench().getProgressService().run(true, true, (org.eclipse.jface.operation.IRunnableWithProgress) op);
                
            } catch (Exception e) {
                System.err.println("PDE Export failed: " + e.getMessage());
                return;
            }

            siteProject.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
            syncSiteXmlWithJars(siteProject, featureId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncSiteXmlWithJars(IProject siteProject, String featureId) throws Exception {
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
                // Extract version from jar name (featureId_1.0.0.jar -> 1.0.0)
                String finalVersion = jarName.substring(featureId.length() + 1, jarName.length() - 4);

                updateSiteXmlFinal(siteProject, featureId, jarName, finalVersion);
            }
        }
    }

    private void updateSiteXmlFinal(IProject project, String featureId, String jarName, String version) throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<site>\n" +
                "   <feature url=\"features/" + jarName + "\" id=\"" + featureId + "\" version=\"" + version + "\">\n" +
                "      <category name=\"caret-tools\"/>\n" +
                "   </feature>\n" +
                "   <category-def name=\"caret-tools\" label=\"Caret Extensions\"/>\n" +
                "</site>";
        
        writeFile(project, "site.xml", content, new NullProgressMonitor());
    }
}