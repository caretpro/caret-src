package caret.repository;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import java.io.InputStream;
import java.net.URL;

public class PluginInstallerP2 {

    public void installPlugin(String jarUrl, String symbolicName) {
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        
        System.out.println("Starting process for: " + symbolicName);
        System.out.println("JAR URL: " + jarUrl);

        try {
            Bundle existingBundle = null;
            for (Bundle b : context.getBundles()) {
                if (symbolicName.equals(b.getSymbolicName())) {
                    existingBundle = b;
                    break;
                }
            }

            if (existingBundle != null) {
                System.out.println("Current bundle state: " + existingBundle.getState());
                System.out.println("Updating...");
                try (InputStream in = new URL(jarUrl).openStream()) {
                    existingBundle.update(in);
                    System.out.println("Update executed. Post-update state: " + existingBundle.getState());
                }
            } else {
                System.out.println("Installing new bundle...");
                Bundle newBundle = context.installBundle(jarUrl);
                System.out.println("Bundle installed. ID: " + newBundle.getBundleId());
                
                System.out.println("Attempting to start bundle...");
                newBundle.start();
                System.out.println("Start executed. Current state: " + newBundle.getState());
            }
            
        } catch (Exception e) {
            System.err.println("ERROR DETECTED: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}