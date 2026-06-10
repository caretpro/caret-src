package caret.repository;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import java.io.InputStream;
import java.net.URL;

public class PluginInstaller {

    public void installPlugin(String jarUrl, String symbolicName) {
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        
        try {
            Bundle existingBundle = null;
            for (Bundle b : context.getBundles()) {
                if (b.getSymbolicName().equals(symbolicName)) {
                    existingBundle = b;
                    break;
                }
            }

            if (existingBundle != null) {
                System.out.println("Updating existing bundle: " + symbolicName);
                try (InputStream in = new URL(jarUrl).openStream()) {
                    existingBundle.update(in);
                }
                System.out.println("Update successful.");
            } else {
                System.out.println("Installing new bundle from: " + jarUrl);
                Bundle newBundle = context.installBundle(jarUrl);
                newBundle.start();
                System.out.println("Installation and Start successful.");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to install plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
}