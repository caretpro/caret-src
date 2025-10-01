package caret.contentAssist;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import caret.ChatView;
import caret.project.Resource;
import caret.tool.SyntaxValidator;

public class CustomCompletionProposalComputer implements IJavaCompletionProposalComputer {

	private Image image = null;
	private ContentAssistInvocationContext context;
	ChatView chatView = ChatView.getInstance();
	public CustomCompletionProposalComputer() {
		
	}

	@Override
	public void sessionStarted() {
		try {
			URL url = new URI("platform:/plugin/caret/icons/test_attrib.png").toURL();
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
			image = imageDescriptor.createImage();
		} catch (Exception e) {
			image = null;
		} 
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {
        List<ICompletionProposal> proposals = new ArrayList<>();
        System.out.println("###JAVA COMPUTE PROPOSALS");
		try {
			//String prefix = context.computeIdentifierPrefix().toString().toLowerCase();
			String text = "";
			String [] listAgents = chatView.getContentAssistantAgents();
			ArrayList<IConfigurationElement> agentExtensions = chatView.getAgentExtensions(true);// isLLM
			chatView.setResource(Resource.getSelectedResource());
			for (int i = 0; i < listAgents.length; i++) {
				for (IConfigurationElement agentExtension : agentExtensions) {
					if(agentExtension.getAttribute("name").toLowerCase().equals(listAgents[i].toLowerCase())){
						System.out.println("### "+i+" FOR Agents: "+listAgents[i]+"-"+agentExtension.getAttribute("name"));
						Thread thread =new Thread(new Runnable() {
			  	    	    public void run() {
			  	    	    	chatView.getSuggestion(agentExtension.getAttribute("id"), context);
			  	    	    }
			  	    	});
						thread.start();
						String displayText = "Agent suggestion: "+agentExtension.getAttribute("id");
						proposals.add(i, new CustomCompletionProposal(text, context.getInvocationOffset(), 0,
			                    text.length(), image, displayText, context, agentExtension.getAttribute("id")));
						break;
					}
				}
			}
	        return proposals;
		} catch (Exception e) {
			//e.printStackTrace();
		}
        return proposals;
    }

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
    public void sessionEnded() {
        if (image != null && !image.isDisposed()) {
            image.dispose();
        } 
        System.out.println("#SESSION FINALIZADA");
    }
	
	//WU
	public void getSuggestions(ContentAssistInvocationContext context) {
		try {
			//String prefix = context.computeIdentifierPrefix().toString().toLowerCase();
			String text = "";
			ArrayList<IConfigurationElement> agentExtensions = chatView.getAgentExtensions(true);// isLLM
			for (IConfigurationElement agentExtension : agentExtensions) {
				Thread thread =new Thread(new Runnable() {
	  	    	    public void run() {
	  	    	    	chatView.getSuggestion(agentExtension.getAttribute("id"), context);
	  	    	    }
	  	    	});     
		        thread.start(); 
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
}
