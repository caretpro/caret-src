package caret.repository;

import java.net.URI;
import java.util.Collections;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class P2Installer {

    public void installFromRepository(String repoUrl, String iuId) {
        try {
            IProvisioningAgent agent = getAgent();
            URI uri = new URI(repoUrl);

            IMetadataRepositoryManager metaManager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
            IMetadataRepository repo = metaManager.loadRepository(uri, new NullProgressMonitor());

            IArtifactRepositoryManager artManager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
            artManager.loadRepository(uri, new NullProgressMonitor());

            IInstallableUnit iu = repo.query(QueryUtil.createIUQuery(iuId), new NullProgressMonitor()).iterator().next();

            ProvisioningSession session = new ProvisioningSession(agent);
            InstallOperation op = new InstallOperation(session, Collections.singletonList(iu));
            
            System.out.println("Resolving...");
            if (op.resolveModal(new NullProgressMonitor()).isOK()) {
                ProvisioningJob job = op.getProvisioningJob(new NullProgressMonitor());
                System.out.println("Downloading and installing...");
                if (job.runModal(new NullProgressMonitor()).isOK()) {
                    System.out.println("Success! Please restart Eclipse.");
                 // Ensure UI updates run on the UI Thread
                    Display.getDefault().asyncExec(() -> {
                        boolean restart = MessageDialog.openQuestion(
                            Display.getDefault().getActiveShell(), 
                            "Installation Successful", 
                            "The plugin has been installed successfully. Would you like to restart Eclipse now?"
                        );

                        if (restart) {
                            PlatformUI.getWorkbench().restart();
                        }
                    });
                }
            } else {
                System.out.println("Finished Process.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IProvisioningAgent getAgent() {
        ServiceReference<IProvisioningAgent> ref = FrameworkUtil.getBundle(this.getClass())
                .getBundleContext().getServiceReference(IProvisioningAgent.class);
        return FrameworkUtil.getBundle(this.getClass()).getBundleContext().getService(ref);
    }
}