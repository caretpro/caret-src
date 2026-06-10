package caret.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.File;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;

public class ProjectGenerator {

    public void execute(IProject pluginProject) {
        IProgressMonitor monitor = new NullProgressMonitor();
        try {
            String pluginId = pluginProject.getName();
            String featureId = pluginId + ".feature";
            
            
            IProject featureProj = createBasicProject(pluginId + ".feature", monitor);
            addNature(featureProj, "org.eclipse.pde.FeatureNature", monitor);
            createFeatureXml(featureProj, pluginId, monitor);
            createBuildProperties(featureProj, monitor);
            
            
            IProject siteProj = createBasicProject(pluginId + ".site", monitor);
            addNature(siteProj, "org.eclipse.pde.UpdateSiteNature", monitor);
            createSiteXml(siteProj, featureProj.getName(), monitor);

            System.out.println("Proyectos generados con éxito.");
            
      
            featureProj.refreshLocal(IProject.DEPTH_INFINITE, monitor);
            siteProj.refreshLocal(IProject.DEPTH_INFINITE, monitor);

            buildWithAnt(siteProj, featureId, monitor);

            System.out.println("Proceso Build completado: Update Site generado.");
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    private void buildWithAnt(IProject siteProject, String featureId, IProgressMonitor monitor) {
        try {
            System.out.println("Iniciando generación de JARs para: " + featureId);

            
            AntRunner runner = new AntRunner();
            
            siteProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);

            siteProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);

            syncSiteXmlWithGeneratedJar(siteProject, featureId, monitor);

        } catch (Exception e) {
            System.err.println("Error en la construcción: " + e.getMessage());
        }
    }

    private void syncSiteXmlWithGeneratedJar(IProject siteProject, String featureId, IProgressMonitor monitor) throws Exception {
        siteProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        IFolder featuresFolder = siteProject.getFolder("features");
        
        
        Thread.sleep(1000); 

        File featuresDir = featuresFolder.getLocation().toFile();
        if (featuresDir.exists()) {
            File[] jars = featuresDir.listFiles((dir, name) -> name.startsWith(featureId) && name.endsWith(".jar"));
            
            if (jars != null && jars.length > 0) {
                File latestJar = jars[0];
                for (File f : jars) {
                    if (f.lastModified() > latestJar.lastModified()) latestJar = f;
                }

                String jarName = latestJar.getName();
                String finalVersion = jarName.substring(featureId.length() + 1, jarName.length() - 4);

                updateSiteXmlFinal(siteProject, featureId, jarName, finalVersion);
                siteProject.getFile("site.xml").refreshLocal(IResource.DEPTH_ZERO, monitor);
                System.out.println("Update Site sincronizado con éxito.");
            }
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

    private void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] prevNatures = description.getNatureIds();
        String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = natureId;
        description.setNatureIds(newNatures);
        project.setDescription(description, monitor);
    }

    private void createFeatureXml(IProject project, String pluginId, IProgressMonitor monitor) throws CoreException {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<feature\n");
        sb.append("      id=\"").append(project.getName()).append("\"\n");
        sb.append("      label=\"New\"\n");
        sb.append("      version=\"1.0.0.qualifier\"\n");
        sb.append("      provider-name=\"EPS\">\n\n");
        sb.append("   <description url=\"http://www.example.com/description\">\n");
        sb.append("      [Enter Feature Description here.]\n");
        sb.append("   </description>\n\n");
        sb.append("   <copyright url=\"http://www.example.com/copyright\">\n");
        sb.append("      [Enter Copyright Description here.]\n");
        sb.append("   </copyright>\n\n");
        sb.append("   <license url=\"http://www.example.com/license\">\n");
        sb.append("      [Enter License Description here.]\n");
        sb.append("   </license>\n\n");
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
        sb.append("      <category name=\"tool\"/>\n");
        sb.append("   </feature>\n");
        sb.append("   <category-def name=\"tool\" label=\"tool\"/>\n");
        sb.append("</site>");
        
        writeFile(project, "site.xml", sb.toString(), monitor);
    }
    
    private void createBuildProperties(IProject project, IProgressMonitor monitor) throws CoreException {
        String content = "bin.includes = feature.xml";
        writeFile(project, "build.properties", content, monitor);
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

    public void buildAndSyncUpdateSite(IProject siteProject, String featureId) {
        try {
            IFile siteFile = siteProject.getFile("site.xml");
            if (!siteFile.exists()) return;

            WorkspaceSiteModel model = new WorkspaceSiteModel(siteFile);
            model.load();

            try {
                Class<?> clazz = Class.forName("org.eclipse.pde.internal.core.exports.SiteExportOperation");
                
                Constructor<?> constructor = clazz.getConstructor(
                    ISiteModel[].class, 
                    String.class, 
                    String.class
                );

                Object op = constructor.newInstance(
                    new ISiteModel[] { model }, 
                    siteProject.getLocation().toOSString(), 
                    "Caret: Building Update Site"
                );

                PlatformUI.getWorkbench().getProgressService().run(true, true, (org.eclipse.jface.operation.IRunnableWithProgress) op);
                
            } catch (Exception e) {
                System.err.println("Error al invocar PDE SiteExportOperation: " + e.getMessage());
                return;
            }

            siteProject.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
            
            File featuresDir = siteProject.getFolder("features").getLocation().toFile();
            if (featuresDir.exists() && featuresDir.isDirectory()) {
                File[] jars = featuresDir.listFiles((dir, name) -> name.startsWith(featureId) && name.endsWith(".jar"));
                
                if (jars != null && jars.length > 0) {
                    File latestJar = jars[0];
                    for (File f : jars) {
                        if (f.lastModified() > latestJar.lastModified()) latestJar = f;
                    }

                    String jarName = latestJar.getName();
                    String finalVersion = jarName.substring(featureId.length() + 1, jarName.length() - 4);

                    updateSiteXmlFinal(siteProject, featureId, jarName, finalVersion);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSiteXmlFinal(IProject project, String featureId, String jarName, String version) throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<site>\n" +
                "   <feature url=\"features/" + jarName + "\" id=\"" + featureId + "\" version=\"" + version + "\">\n" +
                "      <category name=\"tool\"/>\n" +
                "   </feature>\n" +
                "   <category-def name=\"tool\" label=\"tool\"/>\n" +
                "</site>";
        
        writeFile(project, "site.xml", content, new NullProgressMonitor());
    }
}