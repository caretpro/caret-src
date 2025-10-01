package caret.extensions;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import caret.agent.AgentInterface;

public class Extensions {

	public AgentExt [] getAgentExt(String extensionName) {
		AgentExt[] agentExts = null;
		IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(extensionName);
		if(extensions.length>0) {
			String[][] agents = new String[extensions.length][2];
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
					try {
						AgentInterface agent = (AgentInterface) element.createExecutableExtension("class");
						agents[i][0] = element.getAttribute("name");
						agents[i][1] = element.getAttribute("id");
						System.out.println("id plugin: "+element.getAttribute("id"));
						System.out.println("name plugin: "+element.getAttribute("name"));
					} catch (Exception e) {
						System.out.println("ERROR LOAD AGENT EXTENSION: "+e.getMessage());			
					}
				}
		}
		return agentExts;
	}
}
