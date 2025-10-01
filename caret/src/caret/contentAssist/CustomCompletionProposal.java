package caret.contentAssist;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import caret.ChatView;
import caret.tool.Util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.IContextInformation;

public final class CustomCompletionProposal implements ICompletionProposal, ICompletionProposalExtension5 {

	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private String fReplacementString;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;
	
	private ContentAssistInvocationContext fInvocationContext;
	
	private String fIdAgent;
	
	ChatView chatView = ChatView.getInstance();

	public CustomCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition) {
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	public CustomCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition, Image image, String displayString, ContentAssistInvocationContext invocationContext, String idAgent) {
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString= replacementString;
		fReplacementOffset= replacementOffset;
		fReplacementLength= replacementLength;
		fCursorPosition= cursorPosition;
		fImage= image;
		fDisplayString= displayString;
		fInvocationContext = invocationContext;
		fIdAgent = idAgent;
	}
	
	@Override
	public void apply(IDocument document) {
		try {
			document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
		} catch (BadLocationException x) {
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return new Point(fReplacementOffset + fCursorPosition, 0);
	}

	@Override
	public IContextInformation getContextInformation() {
		return fContextInformation;
	}

	@Override
	public Image getImage() {
		return fImage;
	}

	@Override
	public String getDisplayString() {
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo() {
		//System.out.println("##GETADD STRING");
		return fAdditionalProposalInfo;
		//Object info= getAdditionalProposalInfo(new NullProgressMonitor());
		//return info == null ? null : info.toString();
	}
	
	@Override
	public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
		//System.out.println("##GETADD OBJECT");
		monitor.beginTask("Getting suggestion", 100);
		monitor.worked(70);
		fReplacementString = chatView.getSuggestion(fIdAgent, fInvocationContext);
        monitor.worked(30);
        //System.out.println("##ADDITIONAL RETURN: "+fReplacementString);
        String html = "";
        try {
			html = Util.highlightJavaSyntax(fReplacementString);
		} catch (Exception e) {
			// TODO: handle exception
		}
        return html;
    }
	
}
